package org.purescript.parser

import com.intellij.psi.tree.TokenSet

abstract class Parsec {
    abstract val canBeEmpty: Boolean
    abstract val canStartWithSet: TokenSet
    abstract fun parse(context: ParserContext): Info
}