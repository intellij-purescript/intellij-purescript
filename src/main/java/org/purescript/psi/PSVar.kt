package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.file.PSFile

class PSVar(node: ASTNode) : PSPsiElement(node) {
    override fun getReferences(): Array<PsiReference> {
        return arrayOf(
            ValueReference(this),
            ParameterReference(this)
        )
    }

    val module: PSModule get() = (containingFile as PSFile).module
}