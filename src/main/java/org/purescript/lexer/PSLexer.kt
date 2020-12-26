package org.purescript.lexer

import com.intellij.lexer.*
import com.intellij.psi.tree.TokenSet
import org.purescript.psi.PSTokens

class PSLexer : LookAheadLexer(
    MergingLexerAdapter(
        MergingLexerAdapter(
            FilterLexer(
                FlexAdapter(org.purescript.lexer._PSLexer(null)),
                FilterLexer.SetFilter(
                    TokenSet.create(
                        PSTokens.MLCOMMENT,
                        PSTokens.SLCOMMENT
                    )
                )
            ),
            TokenSet.create(PSTokens.STRING, PSTokens.STRING_GAP, PSTokens.STRING_ESCAPED)
        ),
        TokenSet.create(PSTokens.WS)
    ))