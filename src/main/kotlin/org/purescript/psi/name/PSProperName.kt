package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * A proper name is any identifier that starts in a capital letter, like
 * - Maybe
 * - Nothing
 * - Just
 * - Eq
 */
class PSProperName(node: ASTNode) : PSPsiElement(node) {
    val moduleName: PSModuleName? get() = findChildByClass(PSModuleName::class.java)
    val identifier: PSProperName
        get() = findChildByClass(PSProperName::class.java)?.identifier ?: this
    override fun getName(): String = text.trim()
}
