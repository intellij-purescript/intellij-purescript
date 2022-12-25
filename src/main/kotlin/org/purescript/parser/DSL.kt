package org.purescript.parser

import com.intellij.psi.tree.IElementType
import org.purescript.parser.Info.Failure
import org.purescript.parser.Info.Success

sealed interface DSL {
    fun or(next: DSL) = Choice(this, next)
    fun sepBy(delimiter: DSL) = Optional(sepBy1(delimiter))
    fun sepBy1(delimiter: DSL) = this + NoneOrMore(delimiter + this)
    infix fun `as`(node: IElementType) = Symbolic(this, node)
    val oneOrMore get() = this + noneOrMore
    val noneOrMore get() = NoneOrMore(this)
    val withRollback get() = Transaction(this)
    fun parse(context: ParserContext): Info
}

operator fun DSL.plus(other: DSL) = Seq(this, other)

data class ElementToken(val token: IElementType) : DSL {
    override fun parse(context: ParserContext): Info = when {
        context.eat(token) -> Success
        else -> Failure
    }
}

data class StringToken(val token: String) : DSL {
    override fun parse(context: ParserContext): Info = when (context.text()) {
        token -> {
            context.advance()
            Success
        }

        else -> Failure
    }
}

data class Seq(val first: DSL, val next: DSL) : DSL {
    override fun parse(context: ParserContext): Info =
        when (first.parse(context)) {
            Success -> next.parse(context)
            Failure -> Failure
        }
}

data class Choice(val first: DSL, val next: DSL) : DSL {

    companion object {
        fun of(vararg all: DSL): DSL {
            return all.reduce { acc, dsl -> Choice(acc, dsl) }
        }
    }

    override fun parse(context: ParserContext): Info =
        when (first.parse(context)) {
            Success -> Success
            Failure -> next.parse(context)
        }
}

data class NoneOrMore(val child: DSL) : DSL {
    override fun parse(context: ParserContext): Info =
        when (child.parse(context)) {
            Failure -> Success
            Success -> parse(context)
        }
}

data class Optional(val child: DSL) : DSL {
    override fun parse(context: ParserContext): Info {
        child.parse(context)
        return Success
    }
}

data class Transaction(val child: DSL) : DSL {
    override fun parse(context: ParserContext): Info {
        val pack = context.start()
        return when (child.parse(context)) {
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
}

data class Reference(val init: DSL.() -> DSL) : DSL {
    override fun parse(context: ParserContext): Info =
        this.init(this).parse(context)
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override fun parse(context: ParserContext): Info {
        val pack = context.start()
        return when (child.parse(context)) {
            Failure -> {
                pack.drop()
                Failure
            }

            Success -> {
                pack.done(symbol)
                Success
            }
        }
    }
} 