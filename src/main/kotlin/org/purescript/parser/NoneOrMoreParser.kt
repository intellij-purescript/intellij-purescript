package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class NoneOrMoreParser(private val p: Parsec) : Parsec() {
    override tailrec fun parse(context: ParserContext): ParserInfo {
        val position = context.position
        val info = p.parse(context)
        return when (info.success) {
            false -> ParserInfo(position, setOf(p), true)
            true -> parse(context)
        }
    }

    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = true
}