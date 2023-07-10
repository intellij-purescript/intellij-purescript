package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class EmptyAdoBlock(node: ASTNode) : PSPsiElement(node), ValueNamespace, Expression {
    override val valueNames: Sequence<PsiNamedElement> get() = emptySequence()
    override fun unify() = unify(childrenOfType<Expression>().single().inferType())
}