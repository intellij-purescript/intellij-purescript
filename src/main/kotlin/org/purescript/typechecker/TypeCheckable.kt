package org.purescript.typechecker

interface TypeCheckable {
    fun checkType(): TypeCheckerType? {
        val usageType = checkUsageType()
        val referenceType = checkReferenceType()
        return usageType
            ?.let { referenceType?.unify(it) }
            ?: usageType
            ?: referenceType
    }

    /**
     * Type, depending on where the name is used
     *  for variables this is due to operations is done on it
     *  for expression this is the same
     */
    fun checkUsageType(): TypeCheckerType? = null

    /**
     * Type, depending on where the name is declared,
     */
    fun checkReferenceType(): TypeCheckerType? = null
}