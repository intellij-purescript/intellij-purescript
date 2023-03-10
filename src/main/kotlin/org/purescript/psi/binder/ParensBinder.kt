package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class ParensBinder(node: ASTNode) : Binder(node) {
    val binderAtomChildren get() = childrenOfType<Binder>() 
}