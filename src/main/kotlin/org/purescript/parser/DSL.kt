@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

interface DSL {
    fun sepBy(delimiter: DSL) = !sepBy1(delimiter)
    fun sepBy1(delimiter: DSL) = this + !+(delimiter + this).heal
    val heal get() = Transaction(this)
    fun relax(message: String) = Relax(this, message)
    fun relaxTo(to: DSL, message: String) = RelaxTo(this, to, message)
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
fun IElementType.fold(start: DSL, next: DSL) = Fold(this, start, next)

data class Fold(val type: IElementType, val start: DSL, val next: DSL) : DSL {
    private val healedNext = next.heal
    override fun parse(builder: PsiBuilder): Boolean {
        var marker = builder.mark()
        val result = start.parse(builder)
        if (result) while (healedNext.parse(builder)) {
            marker.done(type)
            marker = marker.precede()
        }
        marker.drop()
        return result
    }

}

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

data class Lookahead(val next: DSL, val filter: PsiBuilder.() -> Boolean) : DSL {
    override fun parse(builder: PsiBuilder) = builder.filter() && next.parse(builder)
}

data class Capture(val next: (String) -> DSL) : DSL {
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
        fun of(vararg all: DSL): DSL =
            all.reduce { acc, dsl -> Choice(acc, dsl) }
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
    override val heal: Transaction get() = this
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
    private val cache by lazy { this.init() }
    override fun parse(builder: PsiBuilder): Boolean = cache.parse(builder)
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override fun parse(builder: PsiBuilder): Boolean {
        val start = builder.mark()
        return if (child.parse(builder)) {
            start.done(symbol)
            true
        } else {
            start.drop()
            false
        }
    }
}


data class Relax(val dsl: DSL, val message: String) : DSL {
    private val healedDsl = dsl.heal
    override fun parse(builder: PsiBuilder): Boolean {
        return if (healedDsl.parse(builder)) {
            true
        } else {
            builder.error(message)
            true
        }
    }
}

data class RelaxTo(val dsl: DSL, val to: DSL, val message: String) : DSL {
    private val healedDsl = dsl.heal
    override fun parse(builder: PsiBuilder): Boolean {
        return if (healedDsl.parse(builder)) {
            true
        } else {
            val error = builder.mark()
            while (!builder.eof()) {
                val endOfError = builder.mark()
                if (to.parse(builder)) {
                    endOfError.rollbackTo()
                    break
                } else {
                    endOfError.drop()
                }
                builder.advanceLexer()
            }
            error.error(message)
            true
        }
    }
} 