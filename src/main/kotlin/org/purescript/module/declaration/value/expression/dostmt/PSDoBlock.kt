package org.purescript.module.declaration.value.expression.dostmt

import com.intellij.lang.ASTNode
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.PSPsiElement

class PSDoBlock(node: ASTNode) : PSPsiElement(node) {
    val letDeclarations: Array<PSDoNotationLet>
        get() =
        findChildrenByClass(PSDoNotationLet::class.java)
    
    val statements = childrenOfType<DoStatement>()

    val valueDeclarationGroups get () =
            letDeclarations
                .asSequence()
                .flatMap { it.valueDeclarationGroups.asSequence() }
}