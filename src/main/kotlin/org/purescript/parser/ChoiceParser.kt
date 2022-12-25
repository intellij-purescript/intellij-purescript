package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.Info.*

class ChoiceParser(
    private val head: Parsec,
    private val tail: Array<out Parsec>
) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val start = context.position
        val headInfo: Info = head.tryToParse(context)
        if (headInfo !is Failure) return headInfo
        for (p in tail) {
            val info = p.tryToParse(context)
            if (info !is Failure) return info
        }
        return Failure(start, setOf(head, *tail))
    }

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