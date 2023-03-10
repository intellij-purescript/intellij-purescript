package org.purescript.psi.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.Binder

class PSDoNotationValue(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders: List<Binder> get() = listOf()
}