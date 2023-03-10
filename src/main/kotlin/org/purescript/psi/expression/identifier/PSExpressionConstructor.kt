package org.purescript.psi.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.expression.Qualified
import org.purescript.psi.name.PSQualifiedProperName

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

    override fun getReference(): ConstructorReference =
        ConstructorReference(this, qualifiedProperName)
}
