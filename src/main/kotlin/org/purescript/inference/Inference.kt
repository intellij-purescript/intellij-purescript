package org.purescript.inference

sealed interface InferType {
    @JvmInline
    value class Id(val id: Int) : InferType {
        override fun contains(t: Id) = t == this
        override fun toString(): String = "u$id"
    }

    @JvmInline
    value class Constructor(val name: String) : InferType {
        override fun contains(t: Id): Boolean = false
        override fun toString(): String = name
    }

    @JvmInline
    value class Prim(val name: String) : InferType {
        override fun contains(t: Id): Boolean = false
        override fun toString(): String = name
    }

    data class App(val f: InferType, val on: InferType) : InferType {
        private val isFunction get() = f is App && f.f == Function
        override fun contains(t: Id): Boolean = f.contains(t) || on.contains(t)
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

    data class Row(val labels: List<Pair<String, InferType>>) : InferType {
        override fun contains(t: Id) = labels.any { it.second.contains(t) }
        override fun toString() = labels.joinToString(",", "(", ")") {
            "${it.first}::${it.second}"
        }
    }

    data class Constraint(val constraint: InferType, val of: InferType) : InferType {
        override fun contains(t: Id): Boolean = constraint.contains(t) || of.contains(t)
        override fun toString() = "$constraint => $of"
    }

    data class Alias(val name: String, val type: InferType) : InferType {
        override fun contains(t: Id): Boolean = type.contains(t)
        override fun toString(): String = name
    }

    fun app(other: InferType): App = App(this, other)
    fun contains(t: Id): Boolean

    companion object {
        val Char = Prim("Char")
        val Boolean = Prim("Boolean")
        val Int = Prim("Int")
        val Number = Prim("Number")
        val String = Prim("String")
        val Function = Prim("Function")
        val Array = Prim("Array")
        val Record = Prim("Record")
        fun function(parameter: InferType, ret: InferType): App = Function.app(parameter).app(ret)
        fun record(labels: List<Pair<String, InferType>>) = Record.app(Row(labels))
    }
}

data class IdGenerator(private var unknownCounter: Int = 0) {
    fun newId(): InferType.Id = InferType.Id(unknownCounter++)
}

fun Map<InferType.Id, InferType>.substitute(t: InferType): InferType = when (t) {
    is InferType.Prim, is InferType.Constructor -> t
    is InferType.Id ->
        this[t]?.let {
            if (it.contains(t)) throw RecursiveTypeException(it)
            else substitute(it)
        } ?: t

    is InferType.App -> InferType.App(substitute(t.f), substitute(t.on))
    is InferType.Row -> InferType.Row(t.labels.map { it.first to substitute(it.second) })
    is InferType.Constraint -> InferType.Constraint(substitute(t.constraint), substitute(t.of))
    is InferType.Alias -> InferType.Alias(t.name, substitute(t.type))
}
fun MutableMap<InferType.Id, InferType>.unify(x: InferType, y: InferType) {
    val sx = substitute(x)
    val sy = substitute(y)
    when {
        sx == sy -> return
        sx is InferType.Id -> this[sx] = sy
        sy is InferType.Id -> this[sy] = sx
        sx is InferType.App && sy is InferType.App -> {
            unify(sx.f, sy.f)
            unify(sx.on, sy.on)
        }

        sx is InferType.Row && sy is InferType.Row -> {
            for ((xname, xtype) in sx.labels) {
                for ((yname, ytype) in sy.labels) {
                    if (xname == yname) unify(xtype, ytype)
                }
            }
        }

        sx is InferType.Constraint -> unify(sx.of, sy)
        sy is InferType.Constraint -> unify(sx, sy.of)
        sy is InferType.Alias -> unify(sx, sy.type)
        sx is InferType.Alias -> unify(sx.type, sy)
    }
}

/**
 * There should only be one scope per file, so that serializing unknown ids
 * don't get reused in the same file.
 */
sealed interface Scope {
    fun newUnknown(): InferType.Id
    fun unify(x: InferType, y: InferType)
    fun substitute(type: InferType): InferType
    fun lookup(name: String): InferType
    fun declare(name: String): InferType
    fun lookupTypeVar(name: String): InferType
    class Simple(
        private val substitutions: MutableMap<InferType.Id, InferType>,
        private val environment: MutableMap<String, InferType>,
        private val typeVarEnvironment: MutableMap<String, InferType>
    ) : Scope {
        private val idGenerator = IdGenerator()
        override fun newUnknown(): InferType.Id = idGenerator.newId()
        override fun unify(x: InferType, y: InferType) = substitutions.unify(x, y)
        override fun substitute(type: InferType): InferType = substitutions.substitute(type)
        override fun lookup(name: String): InferType = substitute(environment[name] ?: declare(name))
        override fun declare(name: String): InferType = newUnknown().also { environment[name] = it }

        override fun has(name: String): Boolean = environment.contains(name)
        override fun lookupTypeVar(name: String): InferType = typeVarEnvironment.getOrPut(name, ::newUnknown)
        override fun hasTypevar(name: String): Boolean = typeVarEnvironment.contains(name)
    }

    data class Nested(
        val parent: Scope,
        private val environment: MutableMap<String, InferType>,
        private val typeVarEnvironment: MutableMap<String, InferType>
    ) : Scope {
        override fun newUnknown(): InferType.Id = parent.newUnknown()
        override fun unify(x: InferType, y: InferType) = parent.unify(x, y)
        override fun substitute(type: InferType): InferType = parent.substitute(type)
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

    fun inferApp(func: InferType, argument: InferType): InferType {
        val ret = newUnknown()
        unify(func, InferType.function(argument, ret))
        return substitute(ret)
    }

    fun inferAccess(record: InferType, name: String): InferType {
        return newUnknown().also {
            unify(record, InferType.record(listOf(name to it)))
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
    fun infer(scope: Scope): InferType
}

interface ScopeOwner {
    val scope: Scope
}

class RecursiveTypeException(t: InferType) : Exception("$t is recursive")
