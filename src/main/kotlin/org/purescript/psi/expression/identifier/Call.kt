package org.purescript.psi.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.expression.Expression

class Call(node: ASTNode) : PSPsiElement(node), Expression {
    val arguments: Sequence<Argument> get() = 
        childrenOfType<Argument>().asSequence() + 
            ((parent as? Call)?.arguments ?: emptySequence())
}