package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

class Parameters(node: ASTNode) : PSPsiElement(node) {
    val namedDescendant = binderAtoms.flatMap { it.namedDescendant }
    val binderAtoms get() = parameters.flatMap { it.binderAtoms }
    val varBinderParameters get() = parameters.flatMap { it.binderAtoms.filterIsInstance<VarBinder>() }
    val parameters get() = childrenOfType<Parameter>()
}