package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference

class PSImportDeclarationImpl(node: ASTNode) : PSPsiElement(node) {

    override fun getName() = importName?.name

    val importName get() = findChildByClass(PSProperName::class.java)

    override fun getReference(): PsiReference {
        return ModuleReference(this)
    }
}