package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class GuardParser(
    private val p: Parsec,
    private val predicate: (String?) -> Boolean,
    private val errorMessage: String
) : Parsec() {
    override fun parse(context: ParserContext): ParserInfo {
        val pack = context.start()
        val start = context.position
        val info1 = p.parse(context)
        return if (info1.success) {
            val end = context.position
            val text = context.getText(start, end)
            if (!predicate.invoke(text)) {
                ParserInfo(context.position, setOf(), errorMessage, false)
            } else {
                pack.drop()
                info1
            }
        } else {
            pack.rollbackTo()
            info1
        }
    }

    public override fun calcName() = p.name
    override fun calcExpectedName() = p.expectedName
    override val canStartWithSet: TokenSet get() = p.canStartWithSet
    public override fun calcCanBeEmpty() = p.canBeEmpty
}