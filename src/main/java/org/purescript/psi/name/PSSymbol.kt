package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * A operator in parenthesis without qualifier, example `(+)`
 */
class PSSymbol(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text.drop(1).dropLast(1)
}
