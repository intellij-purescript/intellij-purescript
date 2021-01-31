package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

class PSImportDeclarationImpl(node: ASTNode) : PSPsiElement(node) {

    override fun getName() = findChildByClass(PSProperName::class.java)?.name

    override fun getReference(): PsiReference {
        return ModuleReference(this)
    }
}