package org.purescript.typechecker


sealed interface TypeCheckerType {
    data class Unknown(val id: Int) : TypeCheckerType
    data class TypeVar(val name: String) : TypeCheckerType
    data class TypeLevelString(val value: String) : TypeCheckerType
    data class TypeLevelInt(val value: Int) : TypeCheckerType
    data class TypeConstructor(val name: String) : TypeCheckerType {
        override fun toString() = name
    }

    data class TypeApp(val apply: TypeCheckerType, val to: TypeCheckerType) : TypeCheckerType {
        override fun toString() = when ("$apply") {
            "Prim.Function" -> "$to ->"
            else -> "$apply $to"
        }
    }

    data class Row(val labels: List<Pair<String, TypeCheckerType?>>) : TypeCheckerType

    // ForAll a TypeVarVisibility Text (Maybe (Type a)) (Type a) (Maybe SkolemScope)
    data class ForAll(val name: String, val scope: TypeCheckerType) : TypeCheckerType {
        fun removeArgument(argument: TypeCheckerType): TypeCheckerType? {
            val (call, ret) = scope as? TypeApp ?: return null
            val (_ , arg) = (call as? TypeApp) ?: return null
            return when (ret) {
                is TypeConstructor -> ret
                TypeVar(name) -> argument
                else -> TypeConstructor("Prim.Int")
            }
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
}

