package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.binder.Binder

class PSDoNotationBind(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders get() = childrenOfType<Binder>().flatMap { it.descendantBinders }
}