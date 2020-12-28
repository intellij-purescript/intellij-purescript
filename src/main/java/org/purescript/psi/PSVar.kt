package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

class PSVar(node: ASTNode) : PSPsiElement(node) {
    override fun getReferences(): Array<PsiReference> {
        return arrayOf(
            ValueReference(this),
            ParameterReference(this)
        )
    }
}