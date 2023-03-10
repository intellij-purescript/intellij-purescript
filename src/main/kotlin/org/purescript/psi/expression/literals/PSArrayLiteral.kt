package org.purescript.psi.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.expression.PSValue

class PSArrayLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val values get() = findChildrenByClass(PSValue::class.java) 
}