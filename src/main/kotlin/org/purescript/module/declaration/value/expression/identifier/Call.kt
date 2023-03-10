package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement

class Call(node: ASTNode) : PSPsiElement(node), org.purescript.module.declaration.value.expression.Expression {
    val arguments: Sequence<Argument> get() = 
        childrenOfType<Argument>().asSequence() + 
            ((parent as? Call)?.arguments ?: emptySequence())
}