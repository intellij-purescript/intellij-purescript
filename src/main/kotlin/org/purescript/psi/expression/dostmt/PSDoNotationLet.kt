package org.purescript.psi.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.Binder
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node), DoStatement {
    val valueDeclarationGroups: Array<ValueDeclarationGroup>
        get() = findChildrenByClass(ValueDeclarationGroup::class.java)
    override val binders get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
    override val namedElements: List<PsiNamedElement>
        get() = super.namedElements + valueDeclarationGroups
}