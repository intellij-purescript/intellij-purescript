package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class SeqParser(
    private val ps: Array<out Parsec>,
    private val first: Parsec
) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        return ps.fold(first.parse(context)) {info, p ->
            if (info.success) info.merge(p.parse(context))
            else info
        }
    }

    public override fun calcName() = 
        all().joinToString(" ") { it.name }
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

    public override fun calcCanBeEmpty() = 
        all().all { it.canBeEmpty }
}