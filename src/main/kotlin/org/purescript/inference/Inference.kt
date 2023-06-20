package org.purescript.inference

sealed interface Type {
    @JvmInline
    value class Unknown(val id: Int) : Type {
        override fun toString(): String = "u$id"
    }

    @JvmInline
    value class Constructor(val name: String) : Type {
        override fun toString(): String = name
    }
    @JvmInline
    value class Prim(val name: String) : Type {
        override fun toString(): String = name
    }

    @JvmInline
    value class Var(val name: String) : Type {
        override fun toString(): String = name
    }

    data class App(val f: Type, val on: Type) : Type {
        override fun toString() = when{
            f == Function -> "$on ->"
           else -> "$f $on" 
        } 
    }

    fun app(other: Type): App = App(this, other)

    companion object {
        val Char = Prim("Char")
        val Boolean = Prim("Boolean")
        val Int = Prim("Int")
        val Number = Prim("Number")
        val String = Prim("String")
        val Function = Prim("Function")
        val Array = Prim("Array")
        val Row = Prim("Row")
        val Record = Prim("Record")
        fun function(parameter: Type, ret: Type): App = Function.app(parameter).app(ret)
    }
}

fun Map<Type.Unknown, Type>.substitute(t: Type): Type = when (t) {
    is Type.Var, is Type.Prim, is Type.Constructor -> t
    is Type.Unknown -> this[t]?.let { substitute(it) } ?: t
    is Type.App -> Type.App(substitute(t.f), substitute(t.on))
}

/**
 * There should only be one scope per file, so that serializing unknown ids
 * don't get reused in the same file.
 */
class Scope(
    private val substitutions: MutableMap<Type.Unknown, Type>,
    val environment: MutableMap<String, Type>
) {
    private var unknownCounter = 0
    fun newUnknown(): Type.Unknown = Type.Unknown(unknownCounter++)

    fun unify(x: Type, y: Type) {
        val sx = substitute(x)
        val sy = substitute(y)
        when {
            sx == sy -> return
            sx is Type.Unknown -> substitutions[sx] = sy
            sy is Type.Unknown -> substitutions[sy] = sx
            sx is Type.App && sy is Type.App -> {
                unify(sx.f, sy.f)
                unify(sx.on, sy.on)
            }
        }
    }

    fun substitute(type: Type): Type = substitutions.substitute(type)
    fun lookup(name: String): Type = environment.getOrPut(name, ::newUnknown)
    fun inferApp(func: Type, argument: Type): Type {
        val ret = newUnknown()
        unify(func, Type.function(argument, ret))
        return substitute(ret)
    }
}

interface Inferable {
    fun infer(scope: Scope): Type
}

