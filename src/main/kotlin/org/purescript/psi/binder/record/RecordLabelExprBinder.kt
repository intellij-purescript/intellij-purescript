package org.purescript.psi.binder.record

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.binder.Binder

class RecordLabelExprBinder(node: ASTNode) : Binder(node) {
    val binderAtomChildren get() = childrenOfType<Binder>() 
}