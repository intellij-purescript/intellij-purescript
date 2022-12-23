package org.purescript.parser

import com.intellij.psi.tree.TokenSet

class RollbackParser(private val p: Parsec) : Parsec() {
        override fun parse(context: ParserContext) =
            if (!p.canParse(context)) {
                ParserInfo(context.position, setOf(p), false)
            } else {
                val start = context.position
                val pack = context.start()
                val info = p.parse(context)
                if (info.success) {
                    pack.drop()
                    info
                } else {
                    pack.rollbackTo()
                    ParserInfo(start, info.expected, false)
                }
            }

        public override fun calcName() = "try(" + p.name + ")"
        override fun calcExpectedName() = p.expectedName
        override val canStartWithSet: TokenSet get() = p.canStartWithSet
        public override fun calcCanBeEmpty(): Boolean = p.canBeEmpty
    }