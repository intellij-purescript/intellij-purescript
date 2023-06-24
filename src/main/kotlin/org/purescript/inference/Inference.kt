package org.purescript.inference

sealed interface Type {
    @JvmInline
    value class Unknown(val id: Int) : Type {
        override fun contains(t: Unknown) = t == this
        override fun toString(): String = "u$id"
    }

    @JvmInline
    value class Constructor(val name: String) : Type {
        override fun contains(t: Unknown): Boolean = false
        override fun toString(): String = name
    }

    @JvmInline
    value class Prim(val name: String) : Type {
        override fun contains(t: Unknown): Boolean = false
        override fun toString(): String = name
    }

    data class App(val f: Type, val on: Type) : Type {
        private val isFunction get() = f is App && f.f == Function
        override fun contains(t: Unknown): Boolean = f.contains(t) || on.contains(t)
        override fun toString() = when {
            f == Record && on is Row -> on.labels.joinToString(", ", "{ ", " }") {
                "${it.first}::${it.second}"
            }

            f is App && f.f == Function ->
                if (f.on is App && f.on.isFunction) "(${f.on}) -> $on"
                else "${f.on} -> $on"

            else -> "$f $on"
        }
    }

    data class Row(val labels: List<Pair<String, Type>>) : Type {
        override fun contains(t: Unknown) = labels.any { it.second.contains(t) }
        override fun toString() = labels.joinToString(",", "(", ")") {
            "${it.first}::${it.second}"
        }
    }

    data class Constraint(val constraint: Type, val of: Type) : Type {
        override fun contains(t: Unknown): Boolean = constraint.contains(t) || of.contains(t)
        override fun toString() = "$constraint => $of"
    }

    data class Alias(val name: String, val type: Type) : Type {
        override fun contains(t: Unknown): Boolean = type.contains(t)
        override fun toString(): String = name
    }

    fun app(other: Type): App = App(this, other)
    fun contains(t: Unknown): Boolean

    companion object {
        val Char = Prim("Char")
        val Boolean = Prim("Boolean")
        val Int = Prim("Int")
        val Number = Prim("Number")
        val String = Prim("String")
        val Function = Prim("Function")
        val Array = Prim("Array")
        val Record = Prim("Record")
        fun function(parameter: Type, ret: Type): App = Function.app(parameter).app(ret)
        fun record(labels: List<Pair<String, Type>>) = Record.app(Row(labels))
    }
}

data class UnknownGenerator(private var unknownCounter: Int = 0) {
    fun newUnknown(): Type.Unknown = Type.Unknown(unknownCounter++)
}

fun Map<Type.Unknown, Type>.substitute(t: Type): Type = when (t) {
    is Type.Prim, is Type.Constructor -> t
    is Type.Unknown ->
        this[t]?.let {
            if (it.contains(t)) throw RecursiveTypeException(it)
            else substitute(it)
        } ?: t

    is Type.App -> Type.App(substitute(t.f), substitute(t.on))
    is Type.Row -> Type.Row(t.labels.map { it.first to substitute(it.second) })
    is Type.Constraint -> Type.Constraint(substitute(t.constraint), substitute(t.of))
    is Type.Alias -> Type.Alias(t.name, substitute(t.type))
}
fun MutableMap<Type.Unknown, Type>.unify(x: Type, y: Type) {
    val sx = substitute(x)
    val sy = substitute(y)
    when {
        sx == sy -> return
        sx is Type.Unknown -> this[sx] = sy
        sy is Type.Unknown -> this[sy] = sx
        sx is Type.App && sy is Type.App -> {
            unify(sx.f, sy.f)
            unify(sx.on, sy.on)
        }

        sx is Type.Row && sy is Type.Row -> {
            for ((xname, xtype) in sx.labels) {
                for ((yname, ytype) in sy.labels) {
                    if (xname == yname) unify(xtype, ytype)
                }
            }
        }

        sx is Type.Constraint -> unify(sx.of, sy)
        sy is Type.Constraint -> unify(sx, sy.of)
        sy is Type.Alias -> unify(sx, sy.type)
        sx is Type.Alias -> unify(sx.type, sy)
    }
}

/**
 * There should only be one scope per file, so that serializing unknown ids
 * don't get reused in the same file.
 */
sealed interface Scope {
    fun newUnknown(): Type.Unknown
    fun unify(x: Type, y: Type)
    fun substitute(type: Type): Type
    fun lookup(name: String): Type
    fun declare(name: String): Type
    fun lookupTypeVar(name: String): Type
    class Simple(
        private val substitutions: MutableMap<Type.Unknown, Type>,
        private val environment: MutableMap<String, Type>,
        private val typeVarEnvironment: MutableMap<String, Type>
    ) : Scope {
        private val unknownGenerator = UnknownGenerator()
        override fun newUnknown(): Type.Unknown = unknownGenerator.newUnknown()
        override fun unify(x: Type, y: Type) = substitutions.unify(x, y)
        override fun substitute(type: Type): Type = substitutions.substitute(type)
        override fun lookup(name: String): Type = substitute(environment[name] ?: declare(name))
        override fun declare(name: String): Type = newUnknown().also { environment[name] = it }

        override fun has(name: String): Boolean = environment.contains(name)
        override fun lookupTypeVar(name: String): Type = typeVarEnvironment.getOrPut(name, ::newUnknown)
        override fun hasTypevar(name: String): Boolean = typeVarEnvironment.contains(name)
    }

    data class Nested(
        val parent: Scope,
        private val environment: MutableMap<String, Type>,
        private val typeVarEnvironment: MutableMap<String, Type>
    ) : Scope {
        override fun newUnknown(): Type.Unknown = parent.newUnknown()
        override fun unify(x: Type, y: Type) = parent.unify(x, y)
        override fun substitute(type: Type): Type = parent.substitute(type)
        override fun declare(name: String) = newUnknown().also { environment[name] = it }
        override fun lookup(name: String) = substitute(environment[name] ?: when {
            parent.has(name) -> parent.lookupTypeVar(name)
            else -> environment.getOrPut(name, ::newUnknown)
        })

        override fun lookupTypeVar(name: String) = typeVarEnvironment[name] ?: when {
            parent.hasTypevar(name) -> parent.lookupTypeVar(name)
            else -> typeVarEnvironment.getOrPut(name, ::newUnknown)
        }
        override fun has(name: String): Boolean = parent.has(name)
        override fun hasTypevar(name: String): Boolean = parent.hasTypevar(name)
    }

    fun subScope(): Nested = Nested(this, mutableMapOf(), mutableMapOf())

    fun inferApp(func: Type, argument: Type): Type {
        val ret = newUnknown()
        unify(func, Type.function(argument, ret))
        return substitute(ret)
    }

    fun inferAccess(record: Type, name: String): Type {
        return newUnknown().also {
            unify(record, Type.record(listOf(name to it)))
        }
    }

    companion object {
        fun new(): Scope = Simple(
            mutableMapOf(),
            mutableMapOf(),
            mutableMapOf()
        )
    }

    fun has(name: String): Boolean
    fun hasTypevar(name: String): Boolean
}

interface Inferable {
    fun infer(scope: Scope): Type
}

interface ScopeOwner {
    val scope: Scope
}

class RecursiveTypeException(t: Type) : Exception("$t is recursive")
