package org.purescript.module.declaration.value.expression.literals

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiElement

class RecordLabel(node: ASTNode) : PSPsiElement(node) {
    val expressions get() = childrenOfType<Expression>().asSequence()
    val identifier = findNotNullChildByClass(PSIdentifier::class.java)
    val expression = findChildByClass(Expression::class.java)
    override fun getName(): String = identifier.name
}