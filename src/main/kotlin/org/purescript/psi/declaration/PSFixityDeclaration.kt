package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSPsiElement
import org.purescript.psi.name.PSOperatorName

class PSFixityDeclaration(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {
    private val operatorName get() =
        findNotNullChildByClass(PSOperatorName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getTextOffset(): Int = nameIdentifier.textOffset

    override fun getNameIdentifier(): PsiElement {
        return operatorName
    }

    override fun getName(): String {
        return operatorName.name
    }
}