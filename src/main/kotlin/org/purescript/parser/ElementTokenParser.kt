package org.purescript.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class ElementTokenParser(private val tokenType: IElementType) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo = ParserInfo(
        context.position,
        setOf(this),
        context.eat(tokenType)
    )


    override fun calcExpectedName() = setOf(tokenType.toString())
    override val canStartWithSet: TokenSet get() = TokenSet.create(tokenType)
    public override fun calcCanBeEmpty() = false
}

