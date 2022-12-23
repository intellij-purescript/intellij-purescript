package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.ParserInfo.*

class ChoiceParser(
    private val head: Parsec,
    private val tail: Array<out Parsec>
) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val headInfo: ParserInfo = head.tryToParse(context)
        if (headInfo !is Failure) return headInfo
        val failed: MutableList<Failure> = mutableListOf(headInfo)
        for (p in tail) {
            when (val info = p.tryToParse(context)) {
                is Failure -> failed.add(info)
                is Success -> return info
                // TODO(what does a optional choice mean?)
                is ParserInfo.Optional -> return info
            }
        }
        return failed.reduce { acc, parserInfo ->
            Failure(acc.position, acc.expected + parserInfo.expected)
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