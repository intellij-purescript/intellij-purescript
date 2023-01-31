@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

sealed interface DSL {
    fun sepBy(delimiter: DSL) = !sepBy1(delimiter)
    fun sepBy1(delimiter: DSL) = this + !+(delimiter + this)
    val heal get() = Transaction(this)
    fun parse(builder: PsiBuilder): Boolean
}

val IElementType.dsl get() = ElementToken(this)
val String.dsl get() = StringToken(this)
val DSL.dsl get() = this

operator fun DSL.not() = Optional(dsl)
operator fun IElementType.not() = Optional(dsl)
operator fun String.not() = Optional(dsl)

operator fun DSL.unaryPlus() = OneOrMore(this)
operator fun String.unaryPlus() = OneOrMore(this.dsl)

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

data class Lookahead(val next: DSL, val filter: PsiBuilder.() -> Boolean): DSL {
    override fun parse(builder: PsiBuilder) = builder.filter() && next.parse(builder)
}

data class Capture(val next: (String) -> DSL): DSL {
    override fun parse(builder: PsiBuilder): Boolean {
        val tokenText = builder.tokenText ?: return false
        return next(tokenText).parse(builder)
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
data class OneOrMore(val child: DSL) : DSL {
    override fun parse(builder: PsiBuilder): Boolean =
        child.parse(builder) && (parse(builder) || true)
}

@Suppress("KotlinConstantConditions")
data class Optional(val child: DSL) : DSL {

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