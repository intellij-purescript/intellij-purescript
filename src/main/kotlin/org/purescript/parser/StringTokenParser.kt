package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class StringTokenParser(private val token: String) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val start = context.position
        return if (context.text() == token) {
            context.advance()
            Info.Success
        } else {
            Info.Failure(start, setOf(this))
        }
    }

    override fun calcExpectedName() = setOf("\"" + token + "\"")
    override val canStartWithSet: TokenSet = TokenSet.ANY
    public override fun calcCanBeEmpty() = false
}