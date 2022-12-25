package org.purescript.parser

class StringTokenParser(private val token: String) : Parsec() {
    override fun parse(context: ParserContext): Info =
        when (context.text()) {
            token -> {
                context.advance()
                Info.Success
            }

            else -> Info.Failure
        }

}