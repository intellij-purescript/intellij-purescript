package org.purescript.parser

import com.intellij.psi.tree.IElementType

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
    override fun calcExpectedName() = ref!!.expectedName!!
    override fun canStartWith(type: IElementType): Boolean {
        return ref!!.canStartWith(type)
    }

    public override fun calcCanBeEmpty(): Boolean {
        return ref!!.canBeEmpty()
    }
}