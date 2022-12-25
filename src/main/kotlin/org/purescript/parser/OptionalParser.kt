package org.purescript.parser

import org.purescript.parser.Info.Failure
import org.purescript.parser.Info.Optional

class OptionalParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val info1 = p.parse(context)
        return if (info1 is Failure) Optional(info1.position, info1.expected)
        else info1
    }

}