package org.purescript.inference

sealed interface Type {
    @JvmInline
    value class Unknown(val id: Int) : Type

    @JvmInline
    value class Constructor(val name: String) : Type

    @JvmInline
    value class Var(val name: String) : Type
    data class Reference(val modules: List<String>, val type: Constructor) : Type
    data class App(val f: Type, val on: Type) : Type

    fun app(other: Type): App = App(this, other)

    companion object {
        val Int = Reference(listOf("Prim"), Constructor("Int"))
        val Number = Reference(listOf("Prim"), Constructor("Number"))
        val String = Reference(listOf("Prim"), Constructor("String"))
        val Function = Reference(listOf("Prim"), Constructor("Function"))
        val Array = Reference(listOf("Prim"), Constructor("Array"))
        val Row = Reference(listOf("Prim"), Constructor("Row"))
        val Record = Reference(listOf("Prim"), Constructor("Record"))
        fun function(parameter: Type, ret: Type): App =
            Function.app(parameter).app(ret)

    }
}

fun Map<Type.Unknown, Type>.substitute(t: Type): Type = when (t) {
    is Type.Var, is Type.Reference, is Type.Constructor -> t
    is Type.Unknown -> this[t]?.let { substitute(it) } ?: t
    is Type.App -> Type.App(substitute(t.f), substitute(t.on))
}

/**
 * There should only be one scope per file, so that serializing unknown ids
 * don't get reused in the same file. 
 */
class Scope(private val substitutions: MutableMap<Type.Unknown, Type>) {
    private var unknownCounter = 0
    fun newUnknown(): Type.Unknown = Type.Unknown(unknownCounter++)
    
    fun unify(x: Type, y: Type): Unit {
        val sx = substitutions.substitute(x)
        val sy = substitutions.substitute(y)
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
}

interface Inferable {
    fun infer(scope: Scope): Type
}

