package org.purescript.parser

import com.intellij.psi.tree.IElementType
import org.purescript.parser.Info.Failure
import org.purescript.parser.Info.Success

sealed interface DSL {
    operator fun plus(other: DSL) = Seq(this, other)
    fun or(next: DSL) = Choice(this, next)
    fun sepBy(delimiter: DSL) = Optional(sepBy1(delimiter))
    fun sepBy1(delimiter: DSL) = this + NoneOrMore(delimiter + this)
    infix fun `as`(node: IElementType) = Symbolic(this, node)
    val oneOrMore get() = this + noneOrMore
    val noneOrMore get() = NoneOrMore(this)
    val withRollback get() = Transaction(this)
    fun parse(context: ParserContext): Info = when (this) {
        is ElementToken -> when {
            context.eat(this.token) -> Success
            else -> Failure
        }

        is StringToken -> when (context.text()) {
            this.token -> {
                context.advance()
                Success
            }

            else -> Failure
        }

        is Optional -> {
            this.child.parse(context)
            Success
        }

        is Choice -> when (this.first.parse(context)) {
            Success -> Success
            Failure -> this.next.parse(context)
        }

        is Seq -> when (this.first.parse(context)) {
            Success -> this.next.parse(context)
            Failure -> Failure
        }

        is NoneOrMore -> when (this.child.parse(context)) {
            Failure -> Success
            Success -> this.parse(context)
        }

        is Transaction -> {
            val pack = context.start()
            when (this.child.parse(context)) {
                Failure -> {
                    pack.rollbackTo()
                    Failure
                }

                Success -> {
                    pack.drop()
                    Success
                }
            }
        }

        is Reference -> this.init(this).parse(context)

        is Symbolic -> {
            val pack = context.start()
            when (this.child.parse(context)) {
                Failure -> {
                    pack.drop()
                    Failure
                }

                Success -> {
                    pack.done(this.symbol)
                    Success
                }
            }
        }
    }
}

data class ElementToken(val token: IElementType) : DSL

data class StringToken(val token: String) : DSL

data class Seq(val first: DSL, val next: DSL) : DSL

data class Choice(val first: DSL, val next: DSL) : DSL {

    companion object {
        fun of(vararg all: DSL): DSL {
            return all.reduce { acc, dsl -> Choice(acc, dsl) }
        }
    }
}

data class NoneOrMore(val child: DSL) : DSL

data class Optional(val child: DSL) : DSL

data class Transaction(val child: DSL) : DSL

data class Reference(val init: DSL.() -> DSL) : DSL

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL 