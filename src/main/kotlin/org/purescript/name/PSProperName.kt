package org.purescript.name

import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValuesManager.getProjectPsiDependentCache
import org.purescript.psi.PSPsiElement

/**
 * A proper name is any identifier that starts in a capital letter, like
 * - Maybe
 * - Nothing
 * - Just
 * - Eq
 */
class PSProperName(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = text.trim()
}
