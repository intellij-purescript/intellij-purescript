package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSValueDeclaration

/**
 * A where claus un a expression, e.g.
 * ```
 * where x = 1
 * ```
 * in
 * ```
 * f = x
 *   where x = 1
 * ```
 */
class PSExpressionWhere(node: ASTNode) : PSPsiElement(node)  {
    val valueDeclarations get() =
        findChildrenByClass(PSValueDeclaration::class.java)
}