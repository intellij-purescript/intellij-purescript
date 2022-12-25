package org.purescript.parser

class ParsecRef(private val init: Parsec.() -> Parsec) : Parsec() {
    private val ref: Parsec by lazy { this.init() }
    override fun parse(context: ParserContext): Info = ref.parse(context)
}