package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.ValueDeclarationGroup

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node), DoStatement {
    val valueDeclarationGroups: Array<ValueDeclarationGroup>
        get() = findChildrenByClass(ValueDeclarationGroup::class.java)
    override val binders get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
    override val namedElements: List<PsiNamedElement>
        get() = super.namedElements + valueDeclarationGroups
}