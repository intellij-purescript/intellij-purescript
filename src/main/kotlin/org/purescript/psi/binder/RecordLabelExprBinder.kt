package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class RecordLabelExprBinder(node: ASTNode) : Binder(node) {
    val binderAtomChildren get() = childrenOfType<Binder>() 
}