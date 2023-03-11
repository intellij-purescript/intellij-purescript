package org.purescript.module.declaration.value.expression.controll.caseof

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class CaseAlternative(node: ASTNode) : PSPsiElement(node), ValueNamespace {
    override val valueNames get() = childrenOfType<Binder>().asSequence().flatMap { it.namedDescendant }
}