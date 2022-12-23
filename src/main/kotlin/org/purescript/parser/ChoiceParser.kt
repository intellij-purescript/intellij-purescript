package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.ParserInfo.Failure

class ChoiceParser(
    private val head: Parsec,
    private val tail: Array<out Parsec>
) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val start = context.position
        val headInfo: ParserInfo = head.tryToParse(context)
        if (start < context.position || headInfo !is Failure) return headInfo
        val failed = mutableListOf(headInfo)
        for (p in tail) {
            val info = p.tryToParse(context)
            if (start < context.position || info !is Failure) return info
            else failed.add(info)
        }
        return failed.reduce { acc, parserInfo ->
            when {
                acc.position < parserInfo.position -> parserInfo
                parserInfo.position < acc.position -> acc
                else -> ParserInfo.Failure(
                    acc.position,
                    acc.expected + parserInfo.expected
                )
            }
        }
    }

    override fun calcExpectedName(): Set<String> =
        tail.fold(head.expectedName) { acc, parsec -> acc + parsec.expectedName }

    override val canStartWithSet: TokenSet
        by lazy {
            TokenSet.orSet(
                head.canStartWithSet,
                *tail.map { it.canStartWithSet }.toTypedArray()
            )
        }


    public override fun calcCanBeEmpty(): Boolean {
        if (!head.canBeEmpty) {
            return false
        }
        for (parsec in tail) {
            if (!parsec.canBeEmpty) {
                return false
            }
        }
        return true
    }
}