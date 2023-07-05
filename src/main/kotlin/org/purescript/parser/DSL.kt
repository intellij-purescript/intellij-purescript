@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.purescript.psi.PSElementType

interface DSL : Parser {
    fun sepBy(delimiter: DSL) = !sepBy1(delimiter)
    fun sepBy1(delimiter: DSL) = this + !+(delimiter + this).heal
    val heal: DSL get() = Transaction(this)
    fun relax(message: String) = Relax(this, message)
    fun relaxTo(to: DSL, message: String) = RelaxTo(this, to, message)
    val tokenSet: TokenSet?
    fun choices(): List<DSL> = listOf(this)
}

val IElementType.dsl get() = ElementToken(this)
val String.dsl get() = StringToken(this)
val DSL.dsl get() = this

operator fun DSL.not() = Choice(dsl, True)
operator fun IElementType.not() = Choice(dsl, True)
operator fun String.not() = Choice(dsl, True)

operator fun DSL.unaryPlus() = OneOrMore(this)
operator fun String.unaryPlus() = OneOrMore(this.dsl)

operator fun <Psi: PsiElement> PSElementType.HasPsi<Psi>.invoke(dsl: DSL) = Symbolic<Psi>(dsl, this as IElementType)
operator fun <Psi: PsiElement> PSElementType.HasPsi<Psi>.invoke(other: String) = Symbolic<Psi>(other.dsl, this as IElementType)
operator fun <Psi: PsiElement> PSElementType.HasPsi<Psi>.invoke(o: IElementType) = Symbolic<Psi>(o.dsl, this as IElementType)
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

operator fun Sequence.plus(other: DSL) = Sequence(*sequence, other)
operator fun DSL.plus(other: DSL) = Sequence(dsl, other.dsl)
operator fun Sequence.plus(other: String) = Sequence(*sequence, other.dsl)
operator fun DSL.plus(other: String) = Sequence(dsl, other.dsl)
operator fun Sequence.plus(other: IElementType) = Sequence(*sequence, other.dsl)
operator fun DSL.plus(other: IElementType) = Sequence(dsl, other.dsl)
operator fun IElementType.plus(other: DSL) = Sequence(dsl, other.dsl)
operator fun IElementType.plus(other: IElementType) = Sequence(dsl, other.dsl)
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

data class Lookahead(val next: DSL, val filter: PsiBuilder.() -> Boolean) :
    DSL {
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

class Sequence(vararg val sequence: DSL) : DSL {
    override val tokenSet: TokenSet? get() = sequence.firstOrNull()?.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        for (alt in sequence) if (!alt.parse(b)) return false
        return true
    }

}


data class Choice(val first: DSL, val next: DSL) : DSL {
    override fun choices(): List<DSL> = listOf(first, next).flatMap { it.choices() }

    override fun parse(b: PsiBuilder) =
        tokenSet?.contains(b.tokenType) != false &&
                first.parse(b) ||
                next.parse(b)

    override val tokenSet: TokenSet? =
        first.tokenSet?.let { a ->
            next.tokenSet?.let { b ->
                TokenSet.orSet(a, b)
            }
        }

    companion object {
        fun of(vararg all: DSL): DSL {
            val sequences = all.flatMap { it.choices() }
            val tokens = if (sequences.none { it.tokenSet == null }) {
                TokenSet.create(
                    *sequences
                        .flatMap {
                            it.tokenSet?.types?.asSequence() ?: emptySequence()
                        }
                        .toTypedArray<IElementType?>()
                )
            } else {
                null
            }
            val array =
                List<MutableList<DSL>>(Short.MAX_VALUE + 1) { mutableListOf() }
            for (dsl in sequences) when (val dslTokens = dsl.tokenSet) {
                null -> for (ds in array) ds.add(dsl)
                else -> for (t in dslTokens.types) array[t.index.toInt()].add(dsl)
            }
            val table =
                array.map { it.reduce { acc, dsl -> Choice(acc.heal, dsl) } }
                    .toTypedArray<DSL>()
            return OptChoice(sequences, table, tokens)
        }
    }


}

data class OptChoice(val orgChoices: List<DSL>, val table: Array<DSL>, val tokens: TokenSet?) : DSL {
    override fun choices(): List<DSL> = if (orgChoices.size < 4) orgChoices else listOf(this)
    override val tokenSet: TokenSet? = tokens
    override fun parse(b: PsiBuilder): Boolean =
        b.tokenType?.index?.toInt()
            ?.let { table.getOrNull(it)?.parse(b) ?: false }
            ?: false
}


@Suppress("ControlFlowWithEmptyBody")
data class OneOrMore(val child: DSL) : DSL {
    override val tokenSet: TokenSet? get() = child.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        val ret = child.parse(b)
        if (ret) when (val ts = tokenSet) {
            null -> while (child.parse(b));
            else -> while (ts.contains(b.tokenType) != false && child.parse(b));
        }
        return ret
    }
}

object True : DSL {
    override val tokenSet: TokenSet? get() = null
    override fun parse(b: PsiBuilder): Boolean = true
}

data class Transaction(val child: DSL) : DSL {
    override fun choices() = child.choices()
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

data class Symbolic<Tag>(val child: DSL, val symbol: IElementType) : DSL {
    override fun choices() = child.choices().map { Symbolic<Tag>(it, symbol) }
    override val tokenSet = child.tokenSet
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

data class Continuation(val type: IElementType, val init: DSL, val cont: DSL) : DSL {
    //override fun choices() = init.choices().map { Continuation(type, it, cont) }
    override val tokenSet: TokenSet? = init.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        val marker = b.mark()
        return when {
            !init.parse(b) -> {
                marker.drop()
                false
            }

            cont.heal.parse(b) -> {
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