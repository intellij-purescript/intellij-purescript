package org.purescript.parser.dsl

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.DSL
import org.purescript.parser.PSPsiBuilder

data class Capture(override val tokenSet: TokenSet?, val next: (String) -> DSL) : DSL {
    override fun parse(b: PSPsiBuilder): Boolean {
        val tokenText = b.tokenText ?: return false
        return next(tokenText).parse(b)
    }
}