package org.purescript.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

abstract class Parsec {
    val canBeEmpty: Boolean by lazy { calcCanBeEmpty() }
    protected abstract fun calcCanBeEmpty(): Boolean
    abstract val canStartWithSet: TokenSet
    abstract fun parse(context: ParserContext): Info
    operator fun plus(other: Parsec) = SeqParser(arrayOf(other), this)
    fun or(next: Parsec) = ChoiceParser(this, arrayOf(next))
    fun noneOrMore() = NoneOrMoreParser(this)
    infix fun `as`(node: IElementType) = SymbolicParsec(this, node)
    fun withRollback() = RollbackParser(this)
}