package org.purescript.typechecker

sealed interface TypeCheckerType {
    fun call(argument: TypeCheckerType): TypeCheckerType? = null
    fun substitute(varName: String, type: TypeCheckerType): TypeCheckerType = this

    object Unknown : TypeCheckerType
    data class TypeVar(val name: String) : TypeCheckerType {
        override fun toString() = name
        override fun substitute(varName: String, type: TypeCheckerType) =
            if (name == varName) {
                type
            } else {
                this
            }
    }

    data class TypeLevelString(val value: String) : TypeCheckerType
    data class TypeLevelInt(val value: Int) : TypeCheckerType
    data class TypeConstructor(val name: String) : TypeCheckerType {
        override fun toString() = name
    }

    data class TypeApp(val apply: TypeCheckerType, val to: TypeCheckerType) : TypeCheckerType {
        override fun call(argument: TypeCheckerType): TypeCheckerType? =
            when (val parameter = (apply as? TypeApp)?.to) {
                argument -> to
                is TypeVar -> to.substitute(parameter.name, argument)
                else -> null
            }

        override fun toString() = when ("$apply") {
            "Prim.Function" -> "$to ->"
            else -> "$apply $to"
        }

        override fun substitute(varName: String, type: TypeCheckerType): TypeCheckerType =
            copy(apply.substitute(varName, type), to.substitute(varName, type))
    }

    data class Row(val labels: List<Pair<String, TypeCheckerType?>>) : TypeCheckerType

    // ForAll a TypeVarVisibility Text (Maybe (Type a)) (Type a) (Maybe SkolemScope)
    data class ForAll(val name: String, val scope: TypeCheckerType) : TypeCheckerType {
        override fun call(argument: TypeCheckerType): TypeCheckerType? {
            return scope.call(argument)
        }
    }

    /*
data class TypeWildCard(val value: WildcardData) : TypeCheckerType {
    interface WildcardData
}
data class KindApp(val name: String) : TypeCheckerType
data class ConstrainedTypeCheckerType(val name: String) : TypeCheckerType
data class Skolem(val name: String) : TypeCheckerType
data class KindedType(val label: String) : TypeCheckerType
*/
    companion object {
        fun function(first: TypeCheckerType, vararg arguments: TypeCheckerType): TypeCheckerType =
            arguments.fold(first) { f, a -> TypeApp(TypeApp(Prim.function, f), a) }
    }
}

