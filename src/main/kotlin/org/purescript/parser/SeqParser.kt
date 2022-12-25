package org.purescript.parser

import org.purescript.parser.Info.Failure

class SeqParser(
    private val ps: Array<out Parsec>,
    private val first: Parsec
) : Parsec() {
    override fun parse(context: ParserContext): Info {
        var info = first.parse(context)
        for (p in ps) {
            info = when (info) {
                Failure -> return info
                else -> p.parse(context)
            }
        }
        return info
    }

}