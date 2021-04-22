package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.psi.PSPsiElement
import org.purescript.psi.name.PSIdentifier

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

    override fun getName(): String {
        return identifier.name
    }

    override fun getReference(): PsiReference? {
        return super.getReference()
    }
}