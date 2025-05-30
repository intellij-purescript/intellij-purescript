package org.purescript.parser.dsl

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.DSL
import org.purescript.parser.PSPsiBuilder

class OnOffside(val dsl: DSL):DSL {
    override val tokenSet: TokenSet? get() = dsl.tokenSet
    override fun parse(b: PSPsiBuilder): Boolean =
        b.offside == b.column && dsl.parse(b)
}