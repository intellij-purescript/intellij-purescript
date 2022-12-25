package org.purescript.parser

class StringTokenParser(private val token: String) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val start = context.position
        return if (context.text() == token) {
            context.advance()
            Info.Success
        } else {
            Info.Failure(start, setOf(this))
        }
    }

}