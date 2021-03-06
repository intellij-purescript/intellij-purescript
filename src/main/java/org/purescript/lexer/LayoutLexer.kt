package org.purescript.lexer

import com.intellij.lexer.DelegateLexer
import com.intellij.lexer.Lexer

class LayoutLexer(delegate: Lexer): DelegateLexer(delegate) {
}