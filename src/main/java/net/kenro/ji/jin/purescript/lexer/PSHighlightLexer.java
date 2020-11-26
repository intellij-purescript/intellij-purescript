package net.kenro.ji.jin.purescript.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LookAheadLexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import net.kenro.ji.jin.purescript.psi.PSTokens;

import java.io.Reader;

public final class PSHighlightLexer extends LookAheadLexer {
    public PSHighlightLexer() {
        super(new MergingLexerAdapter(new FlexAdapter(new _PSLexer(null)), TokenSet.create(PSTokens.MLCOMMENT, PSTokens.WS, PSTokens.STRING)), 10);
    }
}

