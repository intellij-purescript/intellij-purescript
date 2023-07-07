@file:Suppress("SimplifyBooleanWithConstants")

package org.purescript.parser

import com.intellij.lang.PsiBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.dsl.ElementToken
import org.purescript.parser.dsl.StringToken
import org.purescript.psi.PSElementType

interface DSL : Parser {
    fun sepBy(delimiter: DSL) = !sepBy1(delimiter)
    fun sepBy1(delimiter: DSL) = this + !+(delimiter + this).heal
    val heal: DSL get() = Transaction(this)
    fun relax(message: String) = Relax(this, message)
    fun relaxTo(to: DSL, message: String) = RelaxTo(this, to, message)
    val tokenSet: TokenSet?
}

val IElementType.dsl get() = ElementToken(this)
val String.dsl get() = StringToken(this)
val DSL.dsl get() = this

operator fun DSL.not() = Choice(dsl, True)
operator fun IElementType.not() = Choice(dsl, True)

operator fun DSL.unaryPlus() = OneOrMore(this)

data class Empty(private val type: IElementType, private val before: DSL) : DSL {
    override val tokenSet: TokenSet? get() = before.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        return b.mark().done(type).let { before.parse(b) }
    }
}

operator fun <Psi : PsiElement> PSElementType.HasPsi<Psi>.invoke(dsl: DSL) = PsiDSL<Psi>(dsl, this as IElementType)
operator fun <Psi : PsiElement> PSElementType.HasPsi<Psi>.invoke(other: String) =
    PsiDSL<Psi>(other.dsl, this as IElementType)

operator fun <Psi : PsiElement> PSElementType.HasPsi<Psi>.invoke(o: IElementType) =
    PsiDSL<Psi>(o.dsl, this as IElementType)

fun IElementType.fold(start: DSL, next: DSL) = Fold(this, start, next)
fun IElementType.cont(start: DSL, next: DSL) = ContinuationMap(start, next to this)

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

    override val tokenSet: TokenSet? by lazy { start.tokenSet }
}

operator fun Sequence.plus(other: Sequence) = Sequence(*sequence, *other.sequence)
operator fun Sequence.plus(other: DSL) = Sequence(*sequence, other)
operator fun DSL.plus(other: Sequence) = Sequence(dsl, *other.sequence)
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


object True : DSL {
    override val heal = this
    override val tokenSet: TokenSet? get() = null
    override fun parse(b: PsiBuilder): Boolean = true
}

class Sequence(vararg sequenceRaw: DSL) : DSL {
    /**
     * the sequence with True removed, since its a noop
     */
    val sequence: Array<DSL> = sequenceRaw.flatMap {
        when (it) {
            is Sequence -> it.sequence.asSequence()
            else -> sequenceOf(it)
        }
    }.filter { it !is True }.toTypedArray()
    override val tokenSet: TokenSet? get() = sequence.firstOrNull()?.tokenSet
    override fun parse(b: PsiBuilder): Boolean {
        for (alt in sequence) if (!alt.parse(b)) {
            return false
        }
        return true
    }

}


class Choice(vararg choicesRaw: DSL) : DSL {
    private val choices: List<DSL> = choicesRaw.flatMap { choice ->
        when (choice) {
            is Choice -> choice.choices
            is Sequence -> fanSequence(choice)
            is Transaction -> when (choice.child) {
                is Choice -> choice.child.choices
                else -> listOf(choice)
            }

            is PsiDSL<*> -> when (choice.child) {
                is Choice -> choice.child.choices.map { PsiDSL<PsiElement>(it, choice.symbol) }
                is Sequence -> fanSequence(choice.child).map { PsiDSL<PsiElement>(it, choice.symbol) }
                else -> listOf(choice)
            }

            else -> listOf(choice)
        }
    }

    private fun fanSequence(choice: Sequence) = when (val first = choice.sequence.firstOrNull()) {
        /*
        *  (a | b) + c = (a + c) | (b + c)
        * */
        is Choice -> first.choices.map { Sequence(it, *choice.sequence.drop(1).toTypedArray()) }
        null -> listOf(True)
        else -> listOf(choice)
    }

    private val lookup: Map<IElementType, List<DSL>> by lazy {
        val keys = choices.mapNotNull { it.tokenSet }.flatMap { it.types.asSequence() }.distinct()
        val table = mutableMapOf(*keys.map { it to mutableListOf<DSL>() }.toTypedArray())
        for (choice in choices) {
            if (choice is True) continue
            if (choice is Sequence && choice.sequence.isEmpty()) continue
            when (val types = choice.tokenSet?.types) {
                null -> for ((_, parser) in table)
                    parser.add(choice)

                else -> for (type in types) table[type]!!.add(choice)
            }
        }
        return@lazy table.mapValues { (_, value) ->
            value.let { choices ->
                /*
                * (a + b) | (a + c) -> a + (b | c)
                */
                choices.groupBy {
                    when (it) {
                        is Sequence -> it.sequence.first()
                        else -> it
                    }
                }.map { (key, group) ->
                    group.singleOrNull()
                        ?: (key + Choice(*group.map {
                            when (it) {
                                key -> True
                                is Sequence -> Sequence(*it.sequence.drop(1).toTypedArray())
                                else -> error("This should not be able to happen")
                            }
                        }.toTypedArray()))
                }
            }.toList()
        }
    }

    private val optional: Boolean by lazy { choices.any { it is True } }
    private val linear: List<DSL> by lazy { choices.filter { it !is True && it.tokenSet == null } }
    override fun parse(b: PsiBuilder): Boolean = (lookup[b.tokenType] ?: linear).any { it.heal.parse(b) } || optional
    override val tokenSet by lazy {
        if (choices.none { it.tokenSet == null }) {
            TokenSet.orSet(*choices.mapNotNull { it.tokenSet }.toTypedArray())
        } else null
    }
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

data class Transaction(val child: DSL) : DSL {
    override val heal: Transaction get() = this
    override fun parse(b: PsiBuilder): Boolean {
        if (child.tokenSet?.contains(b.tokenType) == false)
            return false
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

    override val tokenSet by lazy { child.tokenSet }
}

data class Reference(val init: DSL.() -> DSL) : DSL {
    private val cache by lazy { this.init() }
    override fun parse(b: PsiBuilder): Boolean = cache.parse(b)
    override val tokenSet by lazy { cache.tokenSet }
}

data class PsiDSL<Tag>(val child: DSL, val symbol: IElementType) : DSL {
    override val tokenSet by lazy { child.tokenSet }
    override fun parse(b: PsiBuilder): Boolean {
        if (tokenSet?.contains(b.tokenType) == false)
            return false
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

    override val tokenSet: TokenSet? by lazy { dsl.tokenSet }
}

class ContinuationMap(val init: DSL, private vararg val cont: Pair<DSL, IElementType>) : DSL {
    override val tokenSet: TokenSet? by lazy { init.tokenSet }
    override fun parse(b: PsiBuilder): Boolean {
        val marker = b.mark()
        if (!init.parse(b)) {
            marker.drop()
            return false
        }
        for ((choice, type) in cont) {
            if (choice.heal.parse(b)) {
                marker.done(type)
                return true
            }
        }
        marker.drop()
        return true
    }
}