package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.psi.PSPsiElement

class AdoBlock(node: ASTNode) : PSPsiElement(node), ValueNamespace {
    val statements = childrenOfType<DoStatement>()
    override val valueNames: Sequence<PsiNamedElement>
        get() = statements.asSequence().flatMap { it.valueNames }
}