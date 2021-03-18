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
    var expectedName: HashSet<String?>? = null
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
    protected abstract fun calcExpectedName(): HashSet<String?>
    operator fun plus(other: Parsec): Parsec {
        return Combinators.seq(this, other)
    }
    fun then(next: Parsec): Parsec {
        return this + next
    }

    fun or(next: Parsec): Parsec {
        return Combinators.choice(this, next)
    }

    fun `as`(node: IElementType): SymbolicParsec {
        return SymbolicParsec(this, node)
    }

    abstract fun canStartWith(type: IElementType): Boolean
    fun canBeEmpty(): Boolean {
        if (canBeEmpty == null) {
            canBeEmpty = calcCanBeEmpty()
        }
        return canBeEmpty!!
    }

    protected abstract fun calcCanBeEmpty(): Boolean
}