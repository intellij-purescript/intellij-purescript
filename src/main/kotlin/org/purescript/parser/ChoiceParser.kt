package org.purescript.parser

import com.intellij.psi.tree.TokenSet
import org.purescript.parser.Info.*

class ChoiceParser(
    private val head: Parsec,
    private val tail: Array<out Parsec>
) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val start = context.position
        val headInfo: Info =
            if (head.canBeEmpty || head.canStartWithSet.contains(context.peek())) {
                head.parse(context)
            } else {
                Failure(context.position, setOf(head))
            }
        if (headInfo !is Failure) return headInfo
        for (p in tail) {
            val info =
                if (p.canBeEmpty || p.canStartWithSet.contains(context.peek())) {
                    p.parse(context)
                } else {
                    Failure(context.position, setOf(p))
                }
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