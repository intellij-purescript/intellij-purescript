package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class StringTokenParser(private val token: String) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo =
        if (context.text() == token) {
            context.advance()
            ParserInfo(context.position, setOf(this), null, true)
        } else {
            ParserInfo(context.position, setOf(this), null, false)
        }

    public override fun calcName() = "\"" + token + "\""
    override fun calcExpectedName() = setOf("\"" + token + "\"")
    override val canStartWithSet: TokenSet get() = TokenSet.ANY
    public override fun calcCanBeEmpty() = false
}