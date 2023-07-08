package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.psi.PSPsiElement

class DoStatements(node: ASTNode) : PSPsiElement(node), DoStatement, ValueNamespace {
    private val children = childrenOfType<DoStatement>()
    override val binders get() = children.flatMap { it.binders }
    override val statements get() = children.flatMap { it.statements }
    override val valueNames: Sequence<PsiNamedElement>
        get() = children.asSequence().flatMap { when(it) {
            is DoStatements -> it.valueNames
            is PSDoNotationLet -> it.valueNames
            is PSDoNotationBind -> it.binders.filterIsInstance<PsiNamedElement>().asSequence()
            else -> emptySequence()
        } }

}