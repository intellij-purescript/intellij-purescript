package org.purescript.lexer

import com.intellij.lexer.FilterLexer
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LookAheadLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.tree.TokenSet.WHITE_SPACE
import org.purescript.parser.*

class PSLexer : LookAheadLexer(
    MergingLexerAdapter(
        CollapsingLexer(
            FilterLexer(
                FlexAdapter(_PSLexer(null)),
                FilterLexer.SetFilter(TokenSet.create(MLCOMMENT, SLCOMMENT))
            ),
            TokenSet.create(STRING, STRING_GAP, STRING_ESCAPED),
            STRING
        ),
        WHITE_SPACE
    ))