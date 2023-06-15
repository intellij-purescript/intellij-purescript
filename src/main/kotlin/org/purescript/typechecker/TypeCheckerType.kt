package org.purescript.typechecker

sealed interface TypeCheckerType {
    val parameter get(): TypeCheckerType? = null

    fun substituteModuleNames(nameMap: Map<String, String>): TypeCheckerType

    /**
     * substitute first argument, returning the type of the resulting call
     */
    fun call(argument: TypeCheckerType): TypeCheckerType? = null
    fun arrow(value: TypeCheckerType): TypeCheckerType = function(this, value)
    fun freeVarNames(): Set<String>
    fun substitute(varName: String, type: TypeCheckerType): TypeCheckerType = this
    fun addForall(): TypeCheckerType =
        freeVarNames().fold(this) { scope, name -> ForAll(name, scope) }

    fun unify(with: TypeCheckerType): TypeCheckerType

    object Unknown : TypeCheckerType {
        override fun freeVarNames() = emptySet<String>()
        override fun unify(with: TypeCheckerType): TypeCheckerType = with
        override fun substituteModuleNames(nameMap: Map<String, String>) = this

    }

    data class TypeVar(val name: String) : TypeCheckerType {
        override fun toString() = name
        override fun substituteModuleNames(nameMap: Map<String, String>) = this

        override fun freeVarNames() = setOf(name)
        override fun substitute(varName: String, type: TypeCheckerType) =
            if (name == varName) {
                type
            } else {
                this
            }

        override fun unify(with: TypeCheckerType): TypeCheckerType = with
    }

    data class TypeLevelString(val value: String) : TypeCheckerType {
        override fun substituteModuleNames(nameMap: Map<String, String>) = this
        override fun freeVarNames() = emptySet<String>()
        override fun unify(with: TypeCheckerType): TypeCheckerType = this
    }

    data class TypeLevelInt(val value: Int) : TypeCheckerType {
        override fun substituteModuleNames(nameMap: Map<String, String>) = this
        override fun freeVarNames() = emptySet<String>()
        override fun unify(with: TypeCheckerType): TypeCheckerType = this
    }

    data class TypeConstructor(val moduleName: String, val name: String) : TypeCheckerType {
        constructor(fullName: String) : this(
            fullName.substringBeforeLast("."),
            fullName.substringAfterLast(".")
        )

        override fun substituteModuleNames(nameMap: Map<String, String>) =
            copy(moduleName = nameMap[moduleName] ?: moduleName)

        override fun freeVarNames() = emptySet<String>()
        override fun unify(with: TypeCheckerType): TypeCheckerType = this
        override fun toString() =
            if (moduleName.isEmpty()) name
            else "$moduleName.$name"
    }

    data class TypeApp(val apply: TypeCheckerType, val to: TypeCheckerType) : TypeCheckerType {
        override fun freeVarNames() = apply.freeVarNames() + to.freeVarNames()
        override fun call(argument: TypeCheckerType): TypeCheckerType? =
            when (val parameter = this.parameter) {
                argument -> to
                is TypeVar -> to.substitute(parameter.name, argument)
                else -> null
            }

        override val parameter: TypeCheckerType? get() = (apply as? TypeApp)?.to
        override fun substituteModuleNames(nameMap: Map<String, String>) = TypeApp(
            apply.substituteModuleNames(nameMap),
            to.substituteModuleNames(nameMap)
        )

        override fun toString() = when (apply) {
            Prim.function -> "$to ->"
            TypeConstructor("", "Function") -> "$to ->"
            else -> "$apply $to"
        }

        override fun substitute(varName: String, type: TypeCheckerType) = copy(
            apply = apply.substitute(varName, type),
            to = to.substitute(varName, type)
        )

        // TODO: do deep compare
        override fun unify(with: TypeCheckerType): TypeCheckerType = this
    }

    data class Row(val labels: List<Pair<String, TypeCheckerType?>>) : TypeCheckerType {
        override fun substituteModuleNames(nameMap: Map<String, String>): TypeCheckerType {
            return this
        }

        override fun freeVarNames() = emptySet<String>()
        override fun unify(with: TypeCheckerType): TypeCheckerType = this
    }

    // ForAll a TypeVarVisibility Text (Maybe (Type a)) (Type a) (Maybe SkolemScope)
    data class ForAll(val name: String, val scope: TypeCheckerType) : TypeCheckerType {
        override fun toString(): String = "forall $name. $scope"
        override fun freeVarNames() = scope.freeVarNames() - setOf(name)
        override fun unify(with: TypeCheckerType): TypeCheckerType = this
        override fun call(argument: TypeCheckerType) = scope.call(argument)
        override val parameter: TypeCheckerType? get() = scope.parameter
        override fun substituteModuleNames(nameMap: Map<String, String>) =
            copy(scope = scope.substituteModuleNames(nameMap))
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

