package org.purescript.module.declaration.value.binder.record

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.PSPsiFactory
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.name.PSIdentifier

/**
 * The node `a: 1` in the code
 *
 * ```purescript
 * f {a: 1} = 1
 * ```
 */
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