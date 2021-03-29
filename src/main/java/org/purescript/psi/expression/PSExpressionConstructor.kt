package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSProperName
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
class PSExpressionConstructor(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSProperName] identifying this constructor
     */
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = identifier.name
}
