package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.module.declaration.value.binder.Binder

class PSDoNotationValue(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders: List<Binder> get() = listOf()
}