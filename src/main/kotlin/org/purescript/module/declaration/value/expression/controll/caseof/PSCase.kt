package org.purescript.module.declaration.value.expression.controll.caseof

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSCase(node: ASTNode) : PSPsiElement(node), Expression {
    override val expressions: Sequence<Expression>
        get() = super.expressions +
                childrenOfType<CaseAlternative>().asSequence()
                    .flatMap { it.expressions }
}