package net.kenro.ji.jin.purescript.lexer

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.LookAheadLexer
import com.intellij.lexer.MergingLexerAdapter
import com.intellij.psi.tree.TokenSet
import net.kenro.ji.jin.purescript.psi.PSTokens

class PSHighlightLexer : LookAheadLexer(
    MergingLexerAdapter(
        FlexAdapter(_PSLexer(null)),
        TokenSet.create(PSTokens.MLCOMMENT, PSTokens.WS, PSTokens.STRING)
    ), 10
)