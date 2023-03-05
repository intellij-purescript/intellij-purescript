package org.purescript.psi.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.binder.BinderAtom

class PSDoNotationBind(node: ASTNode) : PSPsiElement(node), DoStatement {
    override val binders get() = childrenOfType<BinderAtom>().flatMap { it.binders }
}