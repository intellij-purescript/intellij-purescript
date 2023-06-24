package org.purescript.module.declaration.value.expression.controll.caseof

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.InferType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSCase(node: ASTNode) : PSPsiElement(node), Expression {
    val alternatives get() = childrenOfType<CaseAlternative>()
    override val expressions: Sequence<Expression>
        get() = super.expressions +
                alternatives.asSequence()
                    .flatMap { it.expressions }
    override fun infer(scope: Scope): InferType {
        TODO("Implement infer for case")
    }
}