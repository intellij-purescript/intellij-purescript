package org.purescript.module.declaration.value.expression.namespace

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.psi.PSPsiElement
import org.purescript.typechecker.TypeCheckerType

class Let(node: ASTNode) : PSPsiElement(node), Expression, ValueNamespace {
    val valueDeclarationGroups get() = childrenOfType<ValueDeclarationGroup>().asSequence()
    val value: PSValue? get() = findChildByClass(PSValue::class.java)
    private val binderChildren get() = childrenOfType<LetBinder>().asSequence()
    private val namedBinders get() = binderChildren.flatMap { it.namedBinders }
    override val valueNames get() = valueDeclarationGroups + namedBinders
    override fun checkType(): TypeCheckerType? = value?.checkType()
}