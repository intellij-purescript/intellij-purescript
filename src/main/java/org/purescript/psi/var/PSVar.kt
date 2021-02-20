package org.purescript.psi.`var`

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.file.PSFile
import org.purescript.psi.PSModule
import org.purescript.psi.PSPsiElement

class PSVar(node: ASTNode) : PSPsiElement(node) {
    override fun getReferences(): Array<PsiReference> {
        return arrayOf(
            ParameterReference(this),
            LocalValueReference(this),
            LocalForeignValueReference(this),
            ImportedValueReference(this)
        )
    }

    override fun getName(): String {
        return text
    }

    val module: PSModule get() = (containingFile as PSFile).module
}