package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.ParserInfo.Failure

class NoneOrMoreParser(private val p: Parsec) : Parsec() {
    override tailrec fun parse(context: ParserContext): ParserInfo {
        val position = context.position
        val info = p.parse(context)
        return when (info !is Failure) {
            false -> ParserInfo.Optional(position, setOf(p))
            true -> parse(context)
        }
    }

    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = true
}