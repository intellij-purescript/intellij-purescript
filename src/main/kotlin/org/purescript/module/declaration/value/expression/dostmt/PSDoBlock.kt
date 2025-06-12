package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class PSDoBlock(node: ASTNode) : PSPsiElement(node), Expression {
    override val expressions get() = childrenOfType<DoStatement>().flatMap { it.expressions }.asSequence()
    override fun unify() {
        val last = childrenOfType<DoStatement>().map { it.inferType() }.lastOrNull()
        last?.let { unify(it) }
    }
}