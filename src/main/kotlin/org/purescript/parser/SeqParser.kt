package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.ParserInfo.Failure

class SeqParser(
    private val ps: Array<out Parsec>,
    private val first: Parsec
) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        var info = first.parse(context)
        for (p in ps) {
            if (!(info !is Failure)) return info
            val other = p.parse(context)
            info = if (info.position == other.position)
                if (other !is Failure) ParserInfo.Optional(
                    info.position,
                    info.expected + other.expected
                )
                else ParserInfo.Failure(
                    info.position,
                    info.expected + other.expected
                )
            else other
        }
        return info
    }

    override fun calcExpectedName(): Set<String> {
        var ret = emptySet<String>()
        for (p in all()) {
            ret = ret + p.expectedName
            if (!p.canBeEmpty) return ret
        }
        return ret
    }

    override val canStartWithSet: TokenSet
        by lazy {
            var ret = TokenSet.EMPTY
            for (p in all()) {
                ret = TokenSet.orSet(ret, p.canStartWithSet)
                if (!p.canBeEmpty) return@lazy ret
            }
            ret
        }
    private fun all() = sequenceOf(first, *ps)
    public override fun calcCanBeEmpty() = all().all { it.canBeEmpty }
}