package org.purescript.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.Combinators.manyOrEmpty
import org.purescript.parser.Combinators.optional

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
    fun sepBy1(delimiter: Parsec) = this + manyOrEmpty(delimiter + this)
    fun sepBy(delimiter: Parsec) = optional(sepBy1(delimiter))
    infix fun `as`(node: IElementType) = SymbolicParsec(this, node)
}