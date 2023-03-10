package org.purescript.psi.binder

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.name.PSIdentifier

/**
 * The node `a` in the code
 * ```purescript
 * f a = 1
 * ```
 */
class VarBinder(node: ASTNode) : Binder(node), PsiNameIdentifierOwner {
    override fun getName(): String = nameIdentifier.name
    override fun getNameIdentifier() = findChildByClass(PSIdentifier::class.java)!!
    override fun getUseScope(): SearchScope = LocalSearchScope(containingFile)
    override fun setName(name: String): PsiElement? {
        val newName = project.service<PSPsiFactory>().createIdentifier(name) ?: return null
        this.nameIdentifier.replace(newName)
        return this
    }
}