package org.purescript.parser

import com.intellij.lang.PsiBuilder
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
    fun parse(psiBuilder: PsiBuilder): Info
}

operator fun DSL.plus(other: DSL) = Seq(this, other)

data class ElementToken(val token: IElementType) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info = when {
        if (psiBuilder.tokenType === token) {
            psiBuilder.advanceLexer()
            true
        } else false -> Success

        else -> Failure
    }
}

data class StringToken(val token: String) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info =
        when (psiBuilder.tokenText ?: "") {
            token -> {
                psiBuilder.advanceLexer()
                Success
            }

            else -> Failure
        }
}

data class Seq(val first: DSL, val next: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info =
        when (first.parse(psiBuilder)) {
            Success -> next.parse(psiBuilder)
            Failure -> Failure
        }
}

data class Choice(val first: DSL, val next: DSL) : DSL {

    companion object {
        fun of(vararg all: DSL): DSL {
            return all.reduce { acc, dsl -> Choice(acc, dsl) }
        }
    }

    override fun parse(psiBuilder: PsiBuilder): Info =
        when (first.parse(psiBuilder)) {
            Success -> Success
            Failure -> next.parse(psiBuilder)
        }
}

data class NoneOrMore(val child: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info =
        when (child.parse(psiBuilder)) {
            Failure -> Success
            Success -> parse(psiBuilder)
        }
}

data class Optional(val child: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info {
        child.parse(psiBuilder)
        return Success
    }
}

data class Transaction(val child: DSL) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info {
        val pack = psiBuilder.mark()
        return when (child.parse(psiBuilder)) {
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
    override fun parse(psiBuilder: PsiBuilder): Info =
        this.init(this).parse(psiBuilder)
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override fun parse(psiBuilder: PsiBuilder): Info {
        val pack = psiBuilder.mark()
        return when (child.parse(psiBuilder)) {
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