package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.Qualified
import org.purescript.name.PSQualifiedProperName
import org.purescript.psi.PSPsiElement

/**
 * A data constructor in an expression, e.g.
 * ```
 * Just
 * ```
 * in
 * ```
 * just3 :: Maybe Int
 * just3 = Just 3
 * ```
 */
class PSExpressionConstructor(node: ASTNode) : PSPsiElement(node), ExpressionAtom, Qualified {

    /**
     * @return the [PSQualifiedProperName] identifying this constructor
     */
    internal val qualifiedProperName: PSQualifiedProperName
        get() = findNotNullChildByClass(PSQualifiedProperName::class.java)

    override fun getName(): String = qualifiedProperName.name
    override val qualifierName get() = qualifiedProperName.moduleName?.name
    override fun getReference(): ConstructorReference = ConstructorReference(this, qualifiedProperName)
    override fun unify() = unify(InferType.Constructor(qualifierName?.let { "$qualifierName.$name" } ?: name))
}
