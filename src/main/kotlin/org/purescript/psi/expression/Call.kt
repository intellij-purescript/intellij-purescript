package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement

class Call(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<Argument> get() = 
        childrenOfType<Argument>().asSequence() + 
            ((parent as? Call)?.arguments ?: emptySequence())
}