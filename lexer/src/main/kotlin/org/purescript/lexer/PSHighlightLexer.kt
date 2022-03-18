package org.purescript.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LookAheadLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.MLCOMMENT
import org.purescript.parser.STRING
import org.purescript.parser.WS

class PSHighlightLexer : LookAheadLexer(
    MergingLexerAdapter(
        FlexAdapter(org.purescript.lexer._PSLexer(null)),
        TokenSet.create(MLCOMMENT, WS, STRING)
    ), 10
)