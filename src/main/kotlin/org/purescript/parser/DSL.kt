package org.purescript.parser

import com.intellij.psi.tree.IElementType

sealed interface DSL {
    fun compile(): Parsec
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
}

class StringToken(private val token: String) : DSL {
    override fun compile() = Combinators.token(token)
}

class Seq(val first: DSL, private vararg val rest: DSL) : DSL {
    override fun compile() = rest.fold(first.compile()) { acc, dsl ->
        Combinators.seq(acc, dsl.compile())
    }
}

class Choice(val first: DSL, private vararg val rest: DSL) : DSL {
    override fun compile(): Parsec {
        return Combinators.choice(
            first.compile(),
            * rest.map { it.compile() }.toTypedArray()
        )
    }
}

class NoneOrMore(private val child: DSL) : DSL {
    override fun compile(): Parsec {
        return child.compile().noneOrMore()
    }
}

class Optional(private val child: DSL) : DSL {
    override fun compile(): Parsec {
        return Combinators.optional(child.compile())
    }
}

class Transaction(private val child: DSL) : DSL {
    override fun compile(): Parsec {
        return child.compile().withRollback()
    }
}

class Reference(private val init: DSL.() -> DSL) : DSL {
    override fun compile(): Parsec =
        Combinators.ref { init(Wrapper(this)).compile() }
}

class Wrapper(private val parsec: Parsec) : DSL {
    override fun compile(): Parsec = parsec
}

class DSLGuard(
    private val child: DSL,
    private val errorMessage: String,
    private val predicate: (String?) -> Boolean
) : DSL {
    override fun compile(): Parsec {
        return Combinators.guard(child.compile(), errorMessage, predicate)
    }
}

class Symbolic(private val child: DSL, val symbol: IElementType) : DSL {
    override fun compile(): Parsec {
        return child.compile().`as`(symbol)
    }
}