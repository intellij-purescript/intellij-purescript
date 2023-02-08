package org.purescript.psi.declaration.signature

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.type.PSType

/**
 * `foo :: int` in
 * ```
 * foo :: Int
 * foo = 42
 * ```
 */
class PSSignature(node: ASTNode) : PSPsiElement(node) {
    val identifier get() =
        findNotNullChildByClass(PSIdentifier::class.java)
    val type get() =
        findNotNullChildByClass(PSType::class.java)
    override fun getName(): String {
        return identifier.name
    }


    val nameIdentifier: PSIdentifier
        get() = findNotNullChildByClass(PSIdentifier::class.java)

    fun setName(name: String) {
        val identifier =
            project.service<PSPsiFactory>().createIdentifier(name)
                ?: return 
        nameIdentifier.replace(identifier)
    }
}