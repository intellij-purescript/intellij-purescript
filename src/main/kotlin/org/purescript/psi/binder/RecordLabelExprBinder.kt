package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType

class RecordLabelExprBinder(node: ASTNode) : BinderAtom(node) {
    val binderAtomChildren get() = childrenOfType<BinderAtom>() 
}