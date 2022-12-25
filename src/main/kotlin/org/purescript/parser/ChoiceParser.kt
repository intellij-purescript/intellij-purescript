package org.purescript.parser

import org.purescript.parser.Info.Failure

class ChoiceParser(
    private val head: Parsec,
    private val tail: Array<out Parsec>
) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val headInfo: Info = head.parse(context)
        if (headInfo != Failure) return headInfo
        for (p in tail) {
            val info = p.parse(context)
            if (info != Failure) return info
        }
        return Failure
    }


}