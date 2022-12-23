package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class OptionalParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val position = context.position
        val info1 = p.parse(context)
        return if (info1.success) info1
        else ParserInfo(
            info1.position,
            info1.expected,
            context.position == position
        )
    }

    public override fun calcName() = "(" + p.name + ")?"
    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = true
}