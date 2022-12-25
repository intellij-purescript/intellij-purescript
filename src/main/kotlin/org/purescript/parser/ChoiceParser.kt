package org.purescript.parser

import org.purescript.parser.Info.Failure

class ChoiceParser(
    private val head: Parsec,
    private val tail: Array<out Parsec>
) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val start = context.position
        val headInfo: Info =  head.parse(context)
        if (headInfo !is Failure) return headInfo
        for (p in tail) {
            val info = p.parse(context)
            if (info !is Failure) return info
        }
        return Failure(start, setOf(head, *tail))
    }


}