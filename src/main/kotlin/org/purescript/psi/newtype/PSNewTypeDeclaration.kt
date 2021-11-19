package org.purescript.psi.newtype

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.name.PSProperName
import org.purescript.psi.PSPsiElement

class PSNewTypeDeclaration(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {

    /**
     * @return the [PSProperName] that identifies this declaration
     */
    private val identifier: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSNewTypeConstructor] defined by this declaration
     */
    val newTypeConstructor: PSNewTypeConstructor
        get() = findNotNullChildByClass(PSNewTypeConstructor::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name

    override fun getTextOffset(): Int = identifier.textOffset
}
