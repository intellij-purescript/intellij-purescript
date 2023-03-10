package org.purescript.module.declaration.signature

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSPsiElement
import org.purescript.name.PSIdentifier
import org.purescript.module.declaration.type.PSType

/**
 * `foo :: int` in
 * ```
 * foo :: Int
 * foo = 42
 * ```
 */
class PSSignature(node: ASTNode) : PSPsiElement(node) {
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    val type get() = findNotNullChildByClass(PSType::class.java)
    override fun getName() = identifier.name
    val nameIdentifier: PSIdentifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    fun setName(name: String) {
        val identifier =
            project.service<PSPsiFactory>().createIdentifier(name)
                ?: return 
        nameIdentifier.replace(identifier)
    }

    override fun getReference(): PsiReference? = 
        parent?.let { PsiReferenceBase.Immediate(this, nameIdentifier.textRangeInParent, it) }
}