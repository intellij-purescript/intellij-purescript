package org.purescript.parser

import com.intellij.psi.tree.IElementType

sealed interface DSL {
    fun compile(): Parsec
    fun optimize(): DSL
    operator fun plus(other: DSL) = Seq(this, other)
    fun or(next: DSL) = Choice(this, next)
    fun sepBy(delimiter: DSL) = Optional(sepBy1(delimiter))
    fun sepBy1(delimiter: DSL) = this + NoneOrMore(delimiter + this)
    fun oneOrMore() = this + noneOrMore()
    fun noneOrMore() = NoneOrMore(this)
    infix fun `as`(node: IElementType) = Symbolic(this, node)
    fun withRollback() = Transaction(this)
}

class ElementToken(private val token: IElementType) : DSL {
    override fun compile() = Combinators.token(token)
    override fun optimize(): DSL = this
}

class StringToken(private val token: String) : DSL {
    override fun compile() = Combinators.token(token)
    override fun optimize(): DSL = this
}

class Seq(val first: DSL, private vararg val rest: DSL) : DSL {
    override fun compile() = rest.fold(first.compile()) { acc, dsl ->
        Combinators.seq(acc, dsl.compile())
    }
    override fun optimize(): DSL = this
}

class Choice(val first: DSL, private vararg val rest: DSL) : DSL {
    override fun compile(): Parsec = Combinators.choice(
        first.compile(),
        * rest.map { it.compile() }.toTypedArray()
    )

    override fun optimize(): DSL = this
}

class NoneOrMore(private val child: DSL) : DSL {
    override fun compile(): Parsec = child.compile().noneOrMore()
    override fun optimize(): DSL = this
}

class Optional(private val child: DSL) : DSL {
    override fun compile(): Parsec = Combinators.optional(child.compile())
    override fun optimize(): DSL = this
}

class Transaction(private val child: DSL) : DSL {
    override fun compile(): Parsec = child.compile().withRollback()
    override fun optimize(): DSL = this
}

class Reference(private val init: DSL.() -> DSL) : DSL {
    override fun compile(): Parsec =
        Combinators.ref { init(Wrapper(this)).compile() }

    override fun optimize(): DSL = this
}

class Wrapper(private val parsec: Parsec) : DSL {
    override fun compile(): Parsec = parsec
    override fun optimize(): DSL = this
}

class DSLGuard(
    private val child: DSL,
    private val errorMessage: String,
    private val predicate: (String?) -> Boolean
) : DSL {
    override fun compile(): Parsec =
        Combinators.guard(child.compile(), errorMessage, predicate)

    override fun optimize(): DSL = this
}

class Symbolic(private val child: DSL, val symbol: IElementType) : DSL {
    override fun compile(): Parsec = child.compile().`as`(symbol)
    override fun optimize(): DSL = this
}