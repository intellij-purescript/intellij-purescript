package org.purescript.psi.`do`

import com.intellij.lang.ASTNode
import org.purescript.psi.PSDoNotationLetImpl
import org.purescript.psi.PSPsiElement

class PSDoBlock(node: ASTNode) : PSPsiElement(node) {
    val letDeclarations get() =
        findChildrenByClass(PSDoNotationLetImpl::class.java)

    val valueDeclarations get () =
            letDeclarations
                .asSequence()
                .flatMap { it.valueDeclarations.asSequence() }
}