package net.kenro.ji.jin.purescript.parser

import com.intellij.psi.tree.IElementType
import java.util.*

class ParsecRef : Parsec() {
    private var ref: Parsec? = null
    fun setRef(ref: Parsec) {
        this.ref = ref
    }

    override fun parse(context: ParserContext): ParserInfo {
        return ref!!.parse(context)
    }

    public override fun calcName(): String {
        return ref!!.name!!
    }

    override fun calcExpectedName(): HashSet<String?> {
        return ref!!.expectedName!!
    }

    override fun canStartWith(type: IElementType): Boolean {
        return ref!!.canStartWith(type)
    }

    public override fun calcCanBeEmpty(): Boolean {
        return ref!!.canBeEmpty()
    }
}