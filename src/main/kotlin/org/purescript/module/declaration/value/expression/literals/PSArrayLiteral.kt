package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.PSValue

class PSArrayLiteral(node: ASTNode) : PSPsiElement(node), ExpressionAtom {
    val values get() = findChildrenByClass(PSValue::class.java) 
}