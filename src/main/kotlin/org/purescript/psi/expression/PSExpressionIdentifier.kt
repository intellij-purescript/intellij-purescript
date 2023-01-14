package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.name.PSQualifiedIdentifier

/**
 * A identifier in an expression, e.g.
 * ```
 * add
 * ```
 * in
 * ```
 * f = add 1 3
 * ```
 */
class PSExpressionIdentifier(node: ASTNode) : PSPsiElement(node), ExpressionAtom {

    /**
     * @return the [PSQualifiedIdentifier] identifying this constructor
     */
    internal val qualifiedIdentifier: PSQualifiedIdentifier
        get() = findNotNullChildByClass(PSQualifiedIdentifier::class.java)
    
    val qualifierName: String? get() = qualifiedIdentifier.moduleName?.name
    
    override fun getName(): String = qualifiedIdentifier.name

    override fun getReference(): ExpressionIdentifierReference =
        ExpressionIdentifierReference(this)
}
