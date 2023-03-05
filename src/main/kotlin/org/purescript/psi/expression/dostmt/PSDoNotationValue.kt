package org.purescript.psi.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.BinderAtom

class PSDoNotationValue(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders: List<BinderAtom> get() = listOf()
}