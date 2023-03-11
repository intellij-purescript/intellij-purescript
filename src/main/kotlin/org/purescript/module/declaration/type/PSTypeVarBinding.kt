package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.util.parentOfType
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

sealed class PSTypeVarBinding(node: ASTNode) : PSPsiElement(node)

class PSTypeVarName(node: ASTNode) : PSTypeVarBinding(node), PsiNameIdentifierOwner {
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getNameIdentifier() = identifier
    override fun getName(): String = identifier.name
    override fun getUseScope() = LocalSearchScope(parentOfType<TypeNamespace>(withSelf = false) ?: containingFile)
    override fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val identifier = factory.createIdentifier(name) ?: return null
        nameIdentifier.replace(identifier)
        return this
    }
}

class PSTypeVarKinded(node: ASTNode) : PSTypeVarBinding(node)
