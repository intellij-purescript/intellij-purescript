package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.psi.PSPsiElement

class PSLet(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    val valueDeclarationGroups get() = childrenOfType<ValueDeclarationGroup>().asSequence()
    val value: PSValue? = findChildByClass(PSValue::class.java)
    private val binderChildren = childrenOfType<LetBinder>().asSequence()
    private val namedBinders = binderChildren.flatMap { it.namedBinders }
    override val valueNames get() = valueDeclarationGroups + namedBinders
}