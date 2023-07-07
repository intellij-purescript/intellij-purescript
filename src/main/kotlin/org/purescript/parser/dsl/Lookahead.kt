package org.purescript.parser.dsl

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.DSL

data class Lookahead(val next: DSL, val filter: PsiBuilder.() -> Boolean) :
    DSL {
    override fun parse(b: PsiBuilder) = b.filter() && next.parse(b)
    override val tokenSet: TokenSet? by lazy { next.tokenSet }
}