package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

class PSImportDeclarationImpl(node: ASTNode) : PSPsiElement(node) {
    val importName: String
        get() = findChildByClass(PSProperName::class.java)?.text?.trim() ?: ""

    override fun getReference(): PsiReference {
        return ModuleReference(this)
    }
}