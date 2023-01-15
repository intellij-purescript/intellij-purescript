package org.purescript.psi.expression.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.declaration.value.ValueDecl

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarations: Array<ValueDecl.Psi>
        get() =
        findChildrenByClass(ValueDecl.Psi::class.java)
}