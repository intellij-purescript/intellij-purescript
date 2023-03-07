package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement

class PSParens(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val value get() = findChildByClass(PSValue::class.java)
}