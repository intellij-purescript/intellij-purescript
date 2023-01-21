package org.purescript.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LookAheadLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.TokenType.WHITE_SPACE
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.MLCOMMENT
import org.purescript.parser.STRING

class PSHighlightLexer : LookAheadLexer(
    MergingLexerAdapter(
        FlexAdapter(_PSLexer(null)),
        TokenSet.create(MLCOMMENT, WHITE_SPACE, STRING)
    ), 10
)