package org.purescript.parser

import com.intellij.psi.tree.IElementType

class ElementTokenParser(private val tokenType: IElementType) : Parsec() {
    override fun parse(context: ParserContext): Info =
        if (context.eat(tokenType)) Info.Success
        else Info.Failure


}

