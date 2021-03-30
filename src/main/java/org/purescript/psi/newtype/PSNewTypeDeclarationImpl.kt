package org.purescript.psi.newtype

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.data.PSDataConstructor

class PSNewTypeDeclarationImpl(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {
    private val identifier: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSDataConstructor] defined by this declaration
     */
    val dataConstructor: PSDataConstructor
        get() = findNotNullChildByClass(PSDataConstructor::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name

    override fun getTextOffset(): Int = identifier.textOffset
}
