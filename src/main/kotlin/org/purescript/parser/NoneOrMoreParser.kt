package org.purescript.parser

import org.purescript.parser.Info.Failure

class NoneOrMoreParser(private val p: Parsec) : Parsec() {
    override tailrec fun parse(context: ParserContext): Info {
        val position = context.position
        return when (p.parse(context)) {
            is Failure -> Info.Optional(position, setOf(p))
            else -> parse(context)
        }
    }

}