package org.purescript.parser

import org.purescript.parser.Info.Failure

class RollbackParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val start = context.position
        val pack = context.start()
        val info = p.parse(context)
        return if (info !is Failure) {
            pack.drop()
            info
        } else {
            pack.rollbackTo()
            Failure(start, info.expected)
        }
    }

}