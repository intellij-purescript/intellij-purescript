package org.purescript.name

import com.intellij.lang.ASTNode
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.purescript.psi.PSPsiElement

/**
 * A proper name is any identifier that starts in a capital letter, like
 * - Maybe
 * - Nothing
 * - Just
 * - Eq
 */
class PSProperName(node: ASTNode) : PSPsiElement(node) {
    override fun getName(): String = CachedValuesManager.getCachedValue(this) { 
        CachedValueProvider.Result.create(text.trim(), this) 
    }
}
