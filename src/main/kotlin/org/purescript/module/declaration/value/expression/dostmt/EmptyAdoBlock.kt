package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class EmptyAdoBlock(node: ASTNode) : PSPsiElement(node), ValueNamespace, Expression {
    override val valueNames: Sequence<PsiNamedElement> get() = emptySequence()
}