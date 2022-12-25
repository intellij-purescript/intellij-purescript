package org.purescript.parser

import org.purescript.parser.Info.Failure

class NoneOrMoreParser(private val p: Parsec) : Parsec() {
    override tailrec fun parse(context: ParserContext): Info =
        when (p.parse(context)) {
            Failure -> Info.Optional
            else -> parse(context)
        }

}