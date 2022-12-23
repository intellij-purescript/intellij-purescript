package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class StringTokenParser(private val token: String) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val start = context.position
        return if (context.text() == token) {
            context.advance()
            ParserInfo.Success(start)
        } else {
            ParserInfo.Failure(start, setOf(this))
        }
    }

    override fun calcExpectedName() = setOf("\"" + token + "\"")
    override val canStartWithSet: TokenSet get() = TokenSet.ANY
    public override fun calcCanBeEmpty() = false
}