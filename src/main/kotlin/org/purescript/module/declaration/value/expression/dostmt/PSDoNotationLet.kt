package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.inference.Inferable
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.namespace.LetBinder
import org.purescript.psi.PSPsiElement

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node), DoStatement, ValueNamespace {
    private val valueDeclarationGroups get() = findChildrenByClass(ValueDeclarationGroup::class.java)
    private val letBinderValueNames get() = childrenOfType<LetBinder>().flatMap { it.valueNames }
    override val expressions get() = childrenOfType<LetBinder>().flatMap { it.expressions }
    override val binders get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
    override val valueNames get() = valueDeclarationGroups.asSequence() + letBinderValueNames.asSequence()
    override fun unify() = children.filterIsInstance<Inferable>().forEach { it.unify() }
}