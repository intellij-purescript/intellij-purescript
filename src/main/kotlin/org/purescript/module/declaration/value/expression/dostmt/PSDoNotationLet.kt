package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.namespace.LetBinder
import org.purescript.psi.PSPsiElement

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val expressions: List<Expression>
        get() = childrenOfType<LetBinder>().flatMap { it.expressions }
    val valueDeclarationGroups: Array<ValueDeclarationGroup>
        get() = findChildrenByClass(ValueDeclarationGroup::class.java)
    override val binders get() = childrenOfType<Binder>().asSequence().flatMap { it.descendantBinders }
    val letBinderValueNames get() = childrenOfType<LetBinder>().asSequence().flatMap { it.valueNames }
    override val valueNames: Sequence<PsiNamedElement>
        get() = super.valueNames + valueDeclarationGroups + letBinderValueNames
}