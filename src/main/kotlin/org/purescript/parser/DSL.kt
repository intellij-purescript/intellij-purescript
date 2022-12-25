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

    companion object {
        fun parse(dsl: DSL, context: ParserContext): Info {
            return when (dsl) {
                is ElementToken -> when {
                    context.eat(dsl.token) -> Success
                    else -> Failure
                }

                is StringToken -> when (context.text()) {
                    dsl.token -> {
                        context.advance()
                        Success
                    }

                    else -> Failure
                }

                is Optional -> {
                    parse(dsl.child, context)
                    Success
                }

                is Choice -> when (parse(dsl.first, context)) {
                    Success -> Success
                    Failure -> parse(dsl.next, context)
                }

                is Seq -> when (parse(dsl.first, context)) {
                    Success -> parse(dsl.next, context)
                    Failure -> Failure
                }

                is NoneOrMore -> when (parse(dsl.child, context)) {
                    Failure -> Success
                    Success -> parse(dsl, context)
                }

                is Transaction -> {
                    val pack = context.start()
                    return when (parse(dsl.child, context)) {
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

                is Reference -> parse(dsl.init(dsl), context)

                is Symbolic -> {
                    val pack = context.start()
                    return when (val info = parse(dsl.child, context)) {
                        Failure -> {
                            pack.drop()
                            Failure
                        }

                        Success -> {
                            pack.done(dsl.symbol)
                            Success
                        }
                    }
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