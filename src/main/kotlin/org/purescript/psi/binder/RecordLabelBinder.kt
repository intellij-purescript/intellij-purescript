package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.name.PSIdentifier

class RecordLabelBinder(node: ASTNode) : Binder(node), PsiNameIdentifierOwner {

    override fun getName(): String = nameIdentifier.name

    override fun setName(name: String): PsiElement? {
        val newName =
            project.service<PSPsiFactory>().createIdentifier(name) ?: return null
        this.nameIdentifier.replace(newName)
        return this
    }
    override fun getNameIdentifier(): PSIdentifier {
        return findChildByClass(PSIdentifier::class.java)!!
    }
}