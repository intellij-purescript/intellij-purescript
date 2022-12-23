package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class NoneOrMoreParser(private val p: Parsec) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        var info = ParserInfo(context.position, setOf(p), true)
        while (!context.eof()) {
            val position = context.position
            info = p.parse(context)
            if (info.success) {
                if (position == context.position) {
                    // TODO: this should not be allowed.
                    val info2 = ParserInfo(
                        context.position,
                        info.expected,
                        false
                    )
                    return info.merge(info2)
                }
            } else {
                return if (position == context.position) {
                    val info2 = ParserInfo(
                        context.position,
                        info.expected,
                        true
                    )
                    info.merge(info2)
                } else {
                    info
                }
            }
        }
        return info
    }

    public override fun calcName() = "(" + p.name + ")*"
    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = true
}