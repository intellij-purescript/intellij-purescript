@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

sealed interface DSL {
    fun sepBy(delimiter: DSL) = Optional(sepBy1(delimiter))
    fun sepBy(sep: IElementType) = Optional(sepBy1(sep))
    fun sepBy1(delimiter: DSL) = this + NoneOrMore(delimiter + this)
    fun sepBy1(sep: IElementType) = this + NoneOrMore(sep.dsl + this)
    val oneOrMore get() = this + noneOrMore
    val noneOrMore get() = NoneOrMore(this)
    val withRollback get() = Transaction(this)
    fun parse(builder: PsiBuilder): Boolean
}

val IElementType.dsl get() = ElementToken(this)
val String.dsl get() = StringToken(this)
val DSL.dsl get() = this

operator fun IElementType.invoke(dsl: DSL) = Symbolic(dsl, this)
operator fun IElementType.invoke(other: String) = Symbolic(other.dsl, this)
operator fun IElementType.invoke(o: IElementType) = Symbolic(o.dsl, this)

operator fun DSL.plus(other: DSL) = Seq(dsl, other.dsl)
operator fun DSL.plus(other: String) = Seq(dsl, other.dsl)
operator fun DSL.plus(other: IElementType) = Seq(dsl, other.dsl)
operator fun IElementType.plus(other: DSL) = Seq(dsl, other.dsl)
operator fun IElementType.plus(other: IElementType) = Seq(dsl, other.dsl)
operator fun DSL.div(other: DSL) = Choice(dsl, other.dsl)
operator fun DSL.div(other: IElementType) = Choice(dsl, other.dsl)
operator fun IElementType.div(other: DSL) = Choice(dsl, other.dsl)
operator fun IElementType.div(other: IElementType) = Choice(dsl, other.dsl)
operator fun String.div(other: DSL) = Choice(dsl, other.dsl)
operator fun DSL.div(other: String) = Choice(dsl, other.dsl)

data class ElementToken(val token: IElementType) : DSL {
    override fun parse(builder: PsiBuilder): Boolean =
        if (builder.tokenType === token) {
            builder.advanceLexer()
            true
        } else {
            false
        }
}

data class StringToken(val token: String) : DSL {
    override fun parse(builder: PsiBuilder): Boolean =
        when (builder.tokenText) {
            token -> {
                builder.advanceLexer()
                true
            }

            else -> false
        }
}

data class Seq(val first: DSL, val next: DSL) : DSL {
    override fun parse(builder: PsiBuilder): Boolean =
        first.parse(builder) && next.parse(builder)
}

data class Choice(val first: DSL, val next: DSL) : DSL {

    companion object {
        fun of(vararg all: DSL): DSL {
            return all.reduce { acc, dsl -> Choice(acc, dsl) }
        }
    }

    override fun parse(builder: PsiBuilder): Boolean =
        first.parse(builder) || next.parse(builder)
}

@Suppress("KotlinConstantConditions")
data class NoneOrMore(val child: DSL) : DSL {
    override fun parse(builder: PsiBuilder): Boolean =
        child.parse(builder) && parse(builder) || true
}

@Suppress("KotlinConstantConditions")
data class Optional(val child: DSL) : DSL {
    constructor(child: IElementType) : this(child.dsl)

    override fun parse(builder: PsiBuilder): Boolean =
        child.parse(builder) || true
}

data class Transaction(val child: DSL) : DSL {
    override fun parse(builder: PsiBuilder): Boolean {
        val pack = builder.mark()
        return when (child.parse(builder)) {
            false -> {
                pack.rollbackTo()
                false
            }

            true -> {
                pack.drop()
                true
            }
        }
    }
}

data class Reference(val init: DSL.() -> DSL) : DSL {
    override fun parse(builder: PsiBuilder): Boolean =
        this.init(this).parse(builder)
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override fun parse(builder: PsiBuilder): Boolean {
        val pack = builder.mark()
        return when (child.parse(builder)) {
            false -> {
                pack.drop()
                false
            }

            true -> {
                pack.done(symbol)
                true
            }
        }
    }
} 