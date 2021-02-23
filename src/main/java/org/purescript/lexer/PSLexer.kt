package org.purescript.lexer

import com.intellij.lexer.FilterLexer
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LookAheadLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import org.purescript.parser.PSTokens

class PSLexer : LookAheadLexer(
    MergingLexerAdapter(
        CollapsingLexer(
            FilterLexer(
                FlexAdapter(org.purescript.lexer._PSLexer(null)),
                FilterLexer.SetFilter(
                    TokenSet.create(
                        PSTokens.MLCOMMENT,
                        PSTokens.SLCOMMENT
                    )
                )
            ),
            TokenSet.create(PSTokens.STRING, PSTokens.STRING_GAP, PSTokens.STRING_ESCAPED),
            PSTokens.STRING
        ),
        TokenSet.create(PSTokens.WS)
    ))