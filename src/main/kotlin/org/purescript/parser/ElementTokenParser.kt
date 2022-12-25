package org.purescript.parser

import com.intellij.psi.tree.IElementType

class ElementTokenParser(private val tokenType: IElementType) : Parsec() {
    override fun parse(context: ParserContext): Info {
        val position = context.position
        val success = context.eat(tokenType)
        return if (success) Info.Success
        else Info.Failure(position, setOf(this))
    }


}

