package org.purescript.parser

import com.intellij.psi.tree.IElementType
import org.purescript.parser.Info.Failure
import org.purescript.parser.Info.Success

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

                is Optional -> when (val info1 = parse(dsl.child, context)) {
                    Failure -> Success
                    else -> info1
                }
                is Choice -> {
                    val headInfo: Info = parse(dsl.first, context)
                    if (headInfo != Failure) return headInfo
                    for (p in dsl.rest) {
                        val info = parse(p, context)
                        if (info != Failure) return info
                    }
                    return Failure
                }
                is Seq -> {
                    var info = parse(dsl.first,context)
                    for (p in dsl.rest) {
                        info = when (info) {
                            Failure -> return info
                            else -> parse(p, context)
                        }
                    }
                    return info
                }
                is NoneOrMore -> {
                    when (parse(dsl.child, context)) {
                        Failure -> Success
                        else -> parse(dsl, context)
                    }
                }
                is Transaction -> {
                    val pack = context.start()
                    return when (val info = parse(dsl.child, context)) {
                        Failure -> {
                            pack.rollbackTo()
                            Failure
                        }
                        else -> {
                            pack.drop()
                            info
                        }
                    }
                }
                is Reference -> {
                    parse(dsl.init(dsl), context )
                }
                is Wrapper -> {
                    dsl.parsec.parse(context)
                }
                is Symbolic -> {
                    val pack = context.start()
                    return when (val info = parse(dsl.child, context)) {
                        Failure -> {
                            pack.drop()
                            info
                        }
                        else -> {
                            pack.done(dsl.symbol)
                            info
                        }
                    }

                }
            }
        }
    }
}

data class ElementToken(val token: IElementType) : DSL {
    override val compile by lazy { ElementTokenParser(token) }
    override val optimize: DSL = this
}

data class StringToken(val token: String) : DSL {
    override val compile by lazy { StringTokenParser(token) }
    override val optimize: DSL = this
}

class Seq(val first: DSL, vararg val rest: DSL) : DSL {
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

class Choice(val first: DSL, vararg val rest: DSL) : DSL {
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

data class NoneOrMore(val child: DSL) : DSL {
    override val compile: Parsec = NoneOrMoreParser(child.compile)
    override val optimize by lazy {
        if (child == child.optimize) this
        else NoneOrMore(child.optimize)
    }
}

data class Optional(val child: DSL) : DSL {
    override val compile: Parsec by lazy { OptionalParser(child.compile) }
    override val optimize by lazy {
        if (child == child.optimize) this
        else Optional(child.optimize)
    }
}

data class Transaction(val child: DSL) : DSL {
    override val compile: Parsec by lazy { RollbackParser(child.compile) }
    override val optimize by lazy {
        if (child == child.optimize) this
        else Transaction(child.optimize)
    }
}

data class Reference(val init: DSL.() -> DSL) : DSL {
    override val compile: Parsec by lazy {
        ParsecRef(fun Parsec.(): Parsec {
            return init(Wrapper(this)).compile
        })
    }

    override val optimize: DSL get() = this
}

data class Wrapper(val parsec: Parsec) : DSL {
    override val compile: Parsec by lazy { parsec }
    override val optimize: DSL get() = this
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override val compile: Parsec by lazy {
        SymbolicParsec(child.compile, symbol)
    }
    override val optimize by lazy {
        if (child == child.optimize) this
        else Symbolic(child.optimize, symbol)
    }
}