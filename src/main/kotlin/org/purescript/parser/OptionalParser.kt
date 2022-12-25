package org.purescript.parser

import org.purescript.parser.Info.Failure
import org.purescript.parser.Info.Optional

class OptionalParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): Info =
        when (val info1 = p.parse(context)) {
            Failure -> Optional
            else -> info1
        }

}