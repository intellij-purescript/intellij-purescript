package org.purescript.module.declaration.value.expression.controll

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.psi.PSPsiElement

class GuardBranch(node: ASTNode) : PSPsiElement(node), ValueNamespace {
    // There should only be one guard, but it might change in the future
    override val valueNames get() = childrenOfType<Guard>().asSequence().flatMap { it.valueNames }
}