package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.psi.PSPsiElement

class PSDoNotationValue(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders get() = emptySequence<Binder>()
}