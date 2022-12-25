package org.purescript.parser

import org.purescript.parser.Info.Failure

class RollbackParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val pack = context.start()
        return when (val info = p.parse(context)) {
            Failure -> {
                pack.rollbackTo()
                Failure
            }
            else -> {
                pack.drop()
                info
            }
        }
    }

}