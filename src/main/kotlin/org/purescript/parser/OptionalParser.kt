package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.ParserInfo.Failure

class OptionalParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val info1 = p.parse(context)
        return if (info1 !is Failure) info1
        else ParserInfo.Optional(info1.position, info1.expected)
    }

    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = true
}