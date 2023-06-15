package org.purescript.module.declaration.value.binder

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.module.declaration.value.Similar
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiFactory
import org.purescript.typechecker.TypeCheckerType

/**
 * The node `a` in the code
 * ```purescript
 * f a = 1
 * ```
 */
class VarBinder(node: ASTNode) : Binder(node), PsiNameIdentifierOwner {
    override fun checkType(): TypeCheckerType = TypeCheckerType.TypeVar(name)
    override fun getName(): String = nameIdentifier.name
    override fun getNameIdentifier() = findChildByClass(PSIdentifier::class.java)!!
    override fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val newName = factory.createIdentifier(name) ?: return null
        nameIdentifier.replace(newName)
        return this
    }

    override fun areSimilarTo(other: Similar): Boolean = false
}