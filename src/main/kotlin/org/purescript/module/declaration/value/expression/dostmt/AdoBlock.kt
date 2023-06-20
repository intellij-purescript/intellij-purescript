package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Scope
import org.purescript.inference.Type
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class AdoBlock(node: ASTNode) : PSPsiElement(node), ValueNamespace, Expression {
    val statements = childrenOfType<DoStatement>()
    override val valueNames: Sequence<PsiNamedElement>
        get() = statements.asSequence().flatMap { it.valueNames }
    override fun infer(scope: Scope): Type {
        TODO("Implement infer for Ado block")
    }
}