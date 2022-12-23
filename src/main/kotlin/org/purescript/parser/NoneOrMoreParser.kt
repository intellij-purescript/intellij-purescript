package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.Info.Failure

class NoneOrMoreParser(private val p: Parsec) : Parsec() {
    override tailrec fun parse(context: ParserContext): Info {
        val position = context.position
        return when (p.parse(context)) {
            is Failure -> Info.Optional(position, setOf(p))
            else -> parse(context)
        }
    }

    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = true
}