package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.suggested.startOffset
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.expression.dostmt.PSDoBlock
import org.purescript.module.declaration.value.expression.namespace.PSLet
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiFactory

/**
 * The node `a` in the code
 * ```purescript
 * f a = 1
 * ```
 */
class VarBinder(node: ASTNode) : Binder(node), PsiNameIdentifierOwner {
    override fun getName(): String = nameIdentifier.name
    override fun getNameIdentifier() = findChildByClass(PSIdentifier::class.java)!!
    override fun getUseScope(): SearchScope {
        val scope = listOfNotNull(
            parentOfType<ValueDeclarationGroup>(),
            parentOfType<PSLet>(),
            parentOfType<PSDoBlock>()
        ).minByOrNull { it.startOffset } ?: containingFile
        return LocalSearchScope(scope)
    }
    override fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val newName = factory.createIdentifier(name) ?: return null
        this.nameIdentifier.replace(newName)
        return this
    }
}