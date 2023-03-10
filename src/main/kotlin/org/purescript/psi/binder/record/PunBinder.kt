package org.purescript.psi.binder.record

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.binder.Binder
import org.purescript.psi.name.PSIdentifier

class PunBinder(node: ASTNode) : Binder(node), PsiNameIdentifierOwner {
    override fun getNameIdentifier(): PSIdentifier = findChildByClass(PSIdentifier::class.java)!!
    override fun getName(): String = nameIdentifier.name
    override fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val newName = factory.createIdentifier(name) ?: return null
        this.nameIdentifier.replace(newName)
        return this
    }
}