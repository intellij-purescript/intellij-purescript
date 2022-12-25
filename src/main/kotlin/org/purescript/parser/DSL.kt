@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType

sealed interface DSL {
    fun or(next: DSL) = Choice(this, next)
    fun sepBy(delimiter: DSL) = Optional(sepBy1(delimiter))
    fun sepBy1(delimiter: DSL) = this + NoneOrMore(delimiter + this)
    infix fun `as`(node: IElementType) = Symbolic(this, node)
    val oneOrMore get() = this + noneOrMore
    val noneOrMore get() = NoneOrMore(this)
    val withRollback get() = Transaction(this)
    fun parse(psiBuilder: PsiBuilder): Boolean
}

operator fun DSL.plus(other: DSL) = Seq(this, other)

data class ElementToken(val token: IElementType) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean = 
        if (psiBuilder.tokenType === token) {
            psiBuilder.advanceLexer()
            true
        } else {
            false
        }
}

data class StringToken(val token: String) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean =
        when (psiBuilder.tokenText) {
            token -> {
                psiBuilder.advanceLexer()
                true
            }

            else -> false
        }
}

data class Seq(val first: DSL, val next: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean =
        first.parse(psiBuilder) && next.parse(psiBuilder)
}

data class Choice(val first: DSL, val next: DSL) : DSL {

    companion object {
        fun of(vararg all: DSL): DSL {
            return all.reduce { acc, dsl -> Choice(acc, dsl) }
        }
    }

    override fun parse(psiBuilder: PsiBuilder): Boolean =
        first.parse(psiBuilder) || next.parse(psiBuilder)
}

data class NoneOrMore(val child: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean =
        child.parse(psiBuilder) && parse(psiBuilder) || true
}

data class Optional(val child: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean =
        child.parse(psiBuilder) || true
}

data class Transaction(val child: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean {
        val pack = psiBuilder.mark()
        return when (child.parse(psiBuilder)) {
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
    override fun parse(psiBuilder: PsiBuilder): Boolean =
        this.init(this).parse(psiBuilder)
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Boolean {
        val pack = psiBuilder.mark()
        return when (child.parse(psiBuilder)) {
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