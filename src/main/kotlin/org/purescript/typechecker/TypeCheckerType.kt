package org.purescript.typechecker


sealed interface TypeCheckerType {
    data class Unknown(val id: Int) : TypeCheckerType
    data class TypeVar(val name: String) : TypeCheckerType
    data class TypeLevelString(val value: String) : TypeCheckerType
    data class TypeLevelInt(val value: Int) : TypeCheckerType
    data class TypeConstructor(val name: String) : TypeCheckerType
    data class TypeApp(val apply: TypeCheckerType, val to: TypeCheckerType) : TypeCheckerType
    data class Row(val labels: List<Pair<String, TypeCheckerType?>>) : TypeCheckerType
    // ForAll a TypeVarVisibility Text (Maybe (Type a)) (Type a) (Maybe SkolemScope)
    data class ForAll(val name: String, val scope: TypeCheckerType) : TypeCheckerType
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

