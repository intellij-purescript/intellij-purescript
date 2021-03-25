package org.purescript.psi.typeconstructor

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement

class PSTypeConstructor(node: ASTNode) : PSPsiElement(node) {
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String {
        return identifier.name
    }

    override fun getReference(): PsiReference {
        return TypeConstructorReference(this)
    }
}