@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

interface DSL {
    fun sepBy(delimiter: DSL) = !sepBy1(delimiter)
    fun sepBy1(delimiter: DSL) = this + !+(delimiter + this).heal
    val heal: DSL get() = Transaction(this)
    fun relax(message: String) = Relax(this, message)
    fun relaxTo(to: DSL, message: String) = RelaxTo(this, to, message)
    fun parse(b: PsiBuilder): Boolean
    val tokenSet: TokenSet?
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
fun IElementType.cont(start: DSL, next: DSL) = Continuation(this, start, next)

data class Fold(val type: IElementType, val start: DSL, val next: DSL) : DSL {
    private val healedNext = next.heal
    override fun parse(b: PsiBuilder): Boolean {
        if (start.tokenSet?.contains(b.tokenType) == false) return false
        var marker = b.mark()
        val result = start.parse(b)
        if (result) while (healedNext.parse(b)) {
            marker.done(type)
            if (healedNext.tokenSet?.contains(b.tokenType) == false) return result
            marker = marker.precede()
        }
        marker.drop()
        return result
    }

    override val tokenSet: TokenSet? = start.tokenSet
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
    override val tokenSet: TokenSet = TokenSet.create(token)
    override val heal get() = this
    override fun parse(b: PsiBuilder): Boolean =
        if (b.tokenType === token) {
            b.advanceLexer()
            true
        } else {
            false
        }
}

data class StringToken(val token: String) : DSL {
    override val heal get() = this
    override val tokenSet: TokenSet? = null
    override fun parse(b: PsiBuilder): Boolean =
        when (b.tokenText) {
            token -> {
                b.advanceLexer()
                true
            }

            else -> false
        }
}

data class Lookahead(val next: DSL, val filter: PsiBuilder.() -> Boolean) : DSL {
    override fun parse(b: PsiBuilder) = b.filter() && next.parse(b)
    override val tokenSet: TokenSet? = next.tokenSet
}

data class Capture(val next: (String) -> DSL) : DSL {
    override val tokenSet: TokenSet? = null
    override fun parse(b: PsiBuilder): Boolean {
        val tokenText = b.tokenText ?: return false
        return next(tokenText).parse(b)
    }
}

data class Seq(val first: DSL, val next: DSL) : DSL {
    override fun parse(b: PsiBuilder) =
        tokenSet?.contains(b.tokenType) != false && first.parse(b) && next.parse(b)

    override val tokenSet: TokenSet? = first.tokenSet
}

data class Choice(val first: DSL, val next: DSL) : DSL {
    override fun parse(b: PsiBuilder) =
        tokenSet?.contains(b.tokenType) != false && first.parse(b) || next.parse(b)

    override val tokenSet: TokenSet? =
        first.tokenSet?.let { a -> next.tokenSet?.let { b -> TokenSet.orSet(a, b) } }

    companion object {
        fun of(vararg all: DSL) = all.reduce { acc, dsl -> Choice(acc, dsl) }
        fun optOf(vararg all: DSL): DSL {
            var currentMap = hashMapOf<IElementType, DSL>()
            var ret : DSL = OptChoice(currentMap)
            for (dsl in all) {
                when (val tokens = dsl.tokenSet) {
                    null -> {
                        currentMap = hashMapOf()
                        ret = ret / dsl / OptChoice(currentMap)
                    }
                    else -> for (type in tokens.types) {
                        currentMap[type] = currentMap[type]?.let { it / dsl } ?: dsl
                    }
                }
            }
            return ret
        }
    }
}

data class OptChoice(val table: Map<IElementType, DSL>) : DSL {
    override val tokenSet: TokenSet = 
        TokenSet.create(*table.keys.map { it }.toTypedArray())
    override fun parse(b: PsiBuilder): Boolean =
        table[b.tokenType]?.parse(b) ?: false
}


@Suppress("ControlFlowWithEmptyBody")
data class OneOrMore(val child: DSL) : DSL {
    override val tokenSet: TokenSet? get() = child.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        val ret = child.parse(b)
        if (ret) while (tokenSet?.contains(b.tokenType) != false && child.parse(b)) {
        }
        return ret
    }
}

@Suppress("KotlinConstantConditions")
data class Optional(val child: DSL) : DSL {
    override fun parse(b: PsiBuilder) = child.parse(b) || true
    override val tokenSet: TokenSet? = null
}

data class Transaction(val child: DSL) : DSL {
    override val heal: Transaction get() = this
    override fun parse(b: PsiBuilder): Boolean {
        if (child.tokenSet?.contains(b.tokenType) == false) return false
        val pack = b.mark()
        return when (child.parse(b)) {
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

    override val tokenSet: TokenSet? = child.tokenSet
}

data class Reference(val init: DSL.() -> DSL) : DSL {
    private val cache by lazy { this.init() }
    override fun parse(b: PsiBuilder): Boolean = cache.parse(b)
    override val tokenSet: TokenSet? = null
}

data class Symbolic(val child: DSL, val symbol: IElementType) : DSL {
    override val tokenSet: TokenSet? = child.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        if (tokenSet?.contains(b.tokenType) == false) return false
        val start = b.mark()
        return if (child.parse(b)) {
            start.done(symbol)
            true
        } else {
            start.drop()
            false
        }
    }
}


data class Relax(val dsl: DSL, val message: String) : DSL {
    override val tokenSet: TokenSet? get() = null
    private val healedDsl = dsl.heal
    override fun parse(b: PsiBuilder): Boolean {
        return if (healedDsl.parse(b)) {
            true
        } else {
            b.error(message)
            true
        }
    }
}

data class RelaxTo(val dsl: DSL, val to: DSL, val message: String) : DSL {
    private val healedDsl = dsl.heal
    override fun parse(b: PsiBuilder): Boolean {
        return if (healedDsl.parse(b)) {
            true
        } else {
            val error = b.mark()
            while (!b.eof()) {
                val endOfError = b.mark()
                if (to.parse(b)) {
                    endOfError.rollbackTo()
                    break
                } else {
                    endOfError.drop()
                }
                b.advanceLexer()
            }
            error.error(message)
            true
        }
    }

    override val tokenSet: TokenSet? = null
}

class Continuation(val type: IElementType, val init: DSL, val continuaton: DSL) : DSL {
    override val tokenSet: TokenSet? = init.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        val marker = b.mark()
        return when {
            !init.parse(b) -> {
                marker.drop()
                false
            }

            continuaton.heal.parse(b) -> {
                marker.done(type)
                true
            }

            else -> {
                marker.drop()
                true
            }
        }
    }
}