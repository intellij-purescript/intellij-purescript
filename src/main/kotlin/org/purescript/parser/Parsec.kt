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
    operator fun plus(other: Parsec) = SeqParser(arrayOf(other), this)
    fun or(next: Parsec) = ChoiceParser(this, arrayOf(next))
    fun sepBy1(delimiter: Parsec) = this + (delimiter + this).noneOrMore()
    fun sepBy(delimiter: Parsec) = OptionalParser(sepBy1(delimiter))
    fun oneOrMore() = this + noneOrMore()
    fun noneOrMore() = NoneOrMoreParser(this)
    infix fun `as`(node: IElementType) = SymbolicParsec(this, node)
    fun withRollback() = RollbackParser(this)
}