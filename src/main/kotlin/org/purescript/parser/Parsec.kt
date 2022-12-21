package org.purescript.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

abstract class Parsec {
    val name: String by lazy { calcName() }
    protected abstract fun calcName(): String
    val expectedName: Set<String> by lazy { calcExpectedName() }
    protected abstract fun calcExpectedName(): Set<String>
    val canBeEmpty: Boolean by lazy { calcCanBeEmpty() }
    protected abstract fun calcCanBeEmpty(): Boolean
    abstract val canStartWithSet: TokenSet
    fun canParse(context: ParserContext): Boolean =
        canBeEmpty || canStartWithSet.contains(context.peek())
    fun tryToParse(context: ParserContext) =
        if (canParse(context)) {
            parse(context)
        } else {
            ParserInfo(context.position, setOf(this), null, false)
        }
    abstract fun parse(context: ParserContext): ParserInfo
    operator fun plus(other: Parsec) = Combinators.seq(this, other)
    fun or(next: Parsec) = Combinators.choice(this, next)
    fun sepBy1(next: Parsec) = Combinators.sepBy1(this, next)
    fun sepBy(parsec: Parsec) = Combinators.sepBy(this, parsec)
    infix fun `as`(node: IElementType) = SymbolicParsec(this, node)
}