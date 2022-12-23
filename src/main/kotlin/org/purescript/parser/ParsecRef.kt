package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class ParsecRef(private val init: Parsec.() -> Parsec) : Parsec() {
    private val ref: Parsec by lazy { this.init() }
    override fun parse(context: ParserContext): Info = ref.parse(context)
    override fun calcExpectedName() = ref.expectedName
    override val canStartWithSet: TokenSet get() = ref.canStartWithSet
    public override fun calcCanBeEmpty() = ref.canBeEmpty
}