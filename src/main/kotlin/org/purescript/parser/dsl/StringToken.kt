package org.purescript.parser.dsl

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.DSL

data class StringToken(val token: String) : DSL {
    override val heal get() = this
    override val tokenSet: TokenSet? = null
    override fun parse(b: PsiBuilder): Boolean =
        when (b.tokenText) {
            token -> {
                b.advanceLexer()
                true
            }

            else -> false
        }
}