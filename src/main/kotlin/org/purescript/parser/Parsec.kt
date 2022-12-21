package org.purescript.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

abstract class Parsec {
    val name: String by lazy { calcName() }
    val expectedName: Set<String> by lazy { calcExpectedName() }
    abstract fun parse(context: ParserContext): ParserInfo
    protected abstract fun calcName(): String
    protected abstract fun calcExpectedName(): Set<String>
    operator fun plus(other: Parsec) = Combinators.seq(this, other)
    fun or(next: Parsec) = Combinators.choice(this, next)
    fun sepBy1(next: Parsec) = Combinators.sepBy1(this, next)
    infix fun `as`(node: IElementType) = SymbolicParsec(this, node)

    abstract val canStartWithSet: TokenSet

    val canBeEmpty: Boolean by lazy { calcCanBeEmpty() }
    protected abstract fun calcCanBeEmpty(): Boolean
    fun sepBy(parsec: Parsec) = Combinators.sepBy(this, parsec)
}