package org.purescript.module.declaration.value.binder.record

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiFactory

/**
 * The node `a: 1` in the code
 *
 * ```purescript
 * f {a: 1} = 1
 * ```
 */
class RecordLabelBinder(node: ASTNode) : Binder(node) {

    override fun getName(): String = nameIdentifier.name

    fun setName(name: String): PsiElement? {
        val newName =
            project.service<PSPsiFactory>().createIdentifier(name) ?: return null
        this.nameIdentifier.replace(newName)
        return this
    }
    val nameIdentifier = findChildByClass(PSIdentifier::class.java)!!
}