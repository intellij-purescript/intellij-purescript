package org.purescript.parser

import com.intellij.psi.tree.IElementType

sealed interface DSL {
    val compile: Parsec
    val optimize: DSL
    operator fun plus(other: DSL) = Seq(this, other)
    fun or(next: DSL) = Choice(this, next)
    fun sepBy(delimiter: DSL) = Optional(sepBy1(delimiter))
    fun sepBy1(delimiter: DSL) = this + NoneOrMore(delimiter + this)
    infix fun `as`(node: IElementType) = Symbolic(this, node)
    val oneOrMore get() = this + noneOrMore
    val noneOrMore get() = NoneOrMore(this)
    val withRollback get() = Transaction(this)
}

class ElementToken(private val token: IElementType) : DSL {
    override val compile by lazy { ElementTokenParser(token) }
    override val optimize: DSL = this
}

class StringToken(private val token: String) : DSL {
    override val compile by lazy { StringTokenParser(token) }
    override val optimize: DSL = this
}

class Seq(val first: DSL, private vararg val rest: DSL) : DSL {
    override val compile by lazy {
        SeqParser(
            rest.map { it.compile }.toTypedArray(),
            first.compile
        )
    }

    override val optimize: DSL by lazy {
        val first = first.optimize
        val rest = rest.map { it.optimize }.flatMap {
            when (it) {
                is Seq -> sequenceOf(it.first, *it.rest)
                else -> sequenceOf(it)
            }
        }.toTypedArray()
        if (this.first == first && this.rest.contentEquals(rest)) this
        else when (first) {
            is Seq -> Seq(first.first, *first.rest, *rest)
            else -> Seq(first, *rest)
        } 
    }
}

class Choice(val first: DSL, private vararg val rest: DSL) : DSL {
    override val compile: Parsec by lazy {
        ChoiceParser(
            first.compile,
            rest.map { it.compile }.toTypedArray()
        )
    }

    override val optimize by lazy {
        val children: Sequence<DSL> = sequenceOf(first, *rest)
            .map { it.optimize }
            .flatMap {
                when (it) {
                    is Choice -> sequenceOf(it.first, *it.rest)
                    else -> sequenceOf(it)
                }
            }
        val first = children.take(1).first()
        val rest = children.toList().toTypedArray()
        if (this.first == first && this.rest.contentEquals(rest)) this
        else Choice(first, *rest)
    }
}

class NoneOrMore(private val child: DSL) : DSL {
    override val compile: Parsec = child.compile.noneOrMore()
    override val optimize by lazy {
        if (child == child.optimize) this
        else NoneOrMore(child.optimize)
    }
}

class Optional(private val child: DSL) : DSL {
    override val compile: Parsec by lazy { OptionalParser(child.compile) }
    override val optimize by lazy {
        if (child == child.optimize) this
        else Optional(child.optimize)
    }
}

class Transaction(private val child: DSL) : DSL {
    override val compile: Parsec by lazy { child.compile.withRollback() }
    override val optimize by lazy {
        if (child == child.optimize) this
        else Transaction(child.optimize)
    }
}

class Reference(private val init: DSL.() -> DSL) : DSL {
    override val compile: Parsec by lazy {
        ParsecRef(fun Parsec.(): Parsec {
            return init(Wrapper(this)).compile
        })
    }

    override val optimize: DSL get() = this
}

class Wrapper(private val parsec: Parsec) : DSL {
    override val compile: Parsec by lazy { parsec }
    override val optimize: DSL get() = this
}

class DSLGuard(
    private val child: DSL,
    private val errorMessage: String,
    private val predicate: (String?) -> Boolean
) : DSL {
    override val compile: Parsec by lazy {
        GuardParser(child.compile, predicate, errorMessage)
    }

    override val optimize: DSL get() = this
}

class Symbolic(private val child: DSL, val symbol: IElementType) : DSL {
    override val compile: Parsec by lazy { child.compile.`as`(symbol) }
    override val optimize by lazy {
        if (child == child.optimize) this
        else Symbolic(child.optimize, symbol)
    }
}