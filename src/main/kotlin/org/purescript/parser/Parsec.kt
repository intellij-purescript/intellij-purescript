package org.purescript.parser

import com.intellij.psi.tree.TokenSet

abstract class Parsec {
    abstract fun parse(context: ParserContext): Info
}