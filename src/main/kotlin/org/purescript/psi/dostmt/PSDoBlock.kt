package org.purescript.psi.dostmt

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

class PSDoBlock(node: ASTNode) : PSPsiElement(node) {
    val letDeclarations get() =
        findChildrenByClass(PSDoNotationLet::class.java)

    val valueDeclarations get () =
            letDeclarations
                .asSequence()
                .flatMap { it.valueDeclarations.asSequence() }
}