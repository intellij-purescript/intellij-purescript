package org.purescript.parser

import com.intellij.psi.tree.IElementType

abstract class Parsec {
    var name: String? = null
        get() {
            if (field == null) {
                field = calcName()
            }
            return field
        }
        private set
    var expectedName: Set<String>? = null
        get() {
            if (field == null) {
                field = calcExpectedName()
            }
            return field
        }
        private set
    private var canBeEmpty: Boolean? = null
    abstract fun parse(context: ParserContext): ParserInfo
    protected abstract fun calcName(): String
    protected abstract fun calcExpectedName(): Set<String>
    operator fun plus(other: Parsec) = Combinators.seq(this, other)
    fun then(next: Parsec) = this + next
    fun or(next: Parsec) = Combinators.choice(this, next)
    fun sepBy1(next: Parsec) = Combinators.sepBy1(this, next)
    fun `as`(node: IElementType) = SymbolicParsec(this, node)
    abstract fun canStartWith(type: IElementType): Boolean
    fun canBeEmpty(): Boolean {
        if (canBeEmpty == null) {
            canBeEmpty = calcCanBeEmpty()
        }
        return canBeEmpty!!
    }
    protected abstract fun calcCanBeEmpty(): Boolean
    fun sepBy(parsec: Parsec) = Combinators.sepBy(this, parsec)
}