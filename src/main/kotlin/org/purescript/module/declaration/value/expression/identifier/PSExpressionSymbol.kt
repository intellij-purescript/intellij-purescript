package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.name.PSQualifiedSymbol

/**
 * A Operator in an expression, e.g.
 * ```
 * P.(+)
 * ```
 * in
 * ```
 * import Prelude as P
 * f = P.(+) 1 3
 * ```
 */
class PSExpressionSymbol(node: ASTNode) : PSPsiElement(node), ExpressionAtom {

    /**
     * @return the [PSQualifiedSymbol] identifying this constructor
     */
    internal val qualifiedSymbol: PSQualifiedSymbol
        get() = findNotNullChildByClass(PSQualifiedSymbol::class.java)

    override fun getName(): String = qualifiedSymbol.name


    override fun getReference() =
        ExpressionSymbolReference(
            this,
            qualifiedSymbol.moduleName,
            qualifiedSymbol.symbol.operator
        )
}