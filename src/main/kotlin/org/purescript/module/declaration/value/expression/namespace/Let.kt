package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.psi.PSPsiElement

class Let(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    val valueDeclarationGroups get() = childrenOfType<ValueDeclarationGroup>().asSequence()
    val value get() = findChildByClass(Expression::class.java)
    private val binderChildren get() = childrenOfType<LetBinder>().asSequence()
    private val namedBinders get() = binderChildren.flatMap { it.namedBinders }
    override val valueNames get() = valueDeclarationGroups + namedBinders
    override fun unify() {
        for (valueDeclarationGroup in valueDeclarationGroups) {
            valueDeclarationGroup.unify()
        }
        unify(value?.inferType()?: error("could not validate let"))
    }
}