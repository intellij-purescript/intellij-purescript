package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner

class PSNewTypeDeclarationImpl(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {
    private val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name

    override fun getTextOffset(): Int = identifier.textOffset
}
