package org.purescript.psi.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.psi.declaration.PSValueDeclaration

class PSDoNotationLet(node: ASTNode) : PSPsiElement(node) {
    val valueDeclarations get() =
        findChildrenByClass(PSValueDeclaration::class.java)
}