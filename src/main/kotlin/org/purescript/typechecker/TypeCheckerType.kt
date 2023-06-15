package org.purescript.typechecker

sealed interface TypeCheckerType {
    /**
     * substitute first argument, returning the type of the resulting call
     */
    fun call(argument: TypeCheckerType): TypeCheckerType? = null
    fun arrow(value: TypeCheckerType): TypeCheckerType = function(this, value)
    fun freeVarNames(): Set<String>
    fun substitute(varName: String, type: TypeCheckerType): TypeCheckerType = this
    fun addForall(): TypeCheckerType = 
        freeVarNames().fold(this) { scope, name -> ForAll(name, scope) }

    object Unknown : TypeCheckerType {
        override fun freeVarNames() = emptySet<String>()
    }

    data class TypeVar(val name: String) : TypeCheckerType {
        override fun toString() = name
        override fun freeVarNames() = setOf(name)
        override fun substitute(varName: String, type: TypeCheckerType) =
            if (name == varName) {
                type
            } else {
                this
            }
    }

    data class TypeLevelString(val value: String) : TypeCheckerType {
        override fun freeVarNames() = emptySet<String>()
    }

    data class TypeLevelInt(val value: Int) : TypeCheckerType {
        override fun freeVarNames() = emptySet<String>()
    }

    data class TypeConstructor(val name: String) : TypeCheckerType {
        override fun freeVarNames() = emptySet<String>()
        override fun toString() = name
    }

    data class TypeApp(val apply: TypeCheckerType, val to: TypeCheckerType) : TypeCheckerType {
        override fun freeVarNames() = apply.freeVarNames() + to.freeVarNames()
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

        override fun substitute(varName: String, type: TypeCheckerType) = copy(
            apply = apply.substitute(varName, type),
            to = to.substitute(varName, type)
        )
    }

    data class Row(val labels: List<Pair<String, TypeCheckerType?>>) : TypeCheckerType {
        override fun freeVarNames() = emptySet<String>()
    }

    // ForAll a TypeVarVisibility Text (Maybe (Type a)) (Type a) (Maybe SkolemScope)
    data class ForAll(val name: String, val scope: TypeCheckerType) : TypeCheckerType {
        override fun toString(): String = "forall $name. $scope"
        override fun freeVarNames() = scope.freeVarNames() - setOf(name)
        override fun call(argument: TypeCheckerType) = scope.call(argument)
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

