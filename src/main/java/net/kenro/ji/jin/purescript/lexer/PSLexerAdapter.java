package net.kenro.ji.jin.purescript.lexer;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class PSLexerAdapter extends FlexAdapter {
    public PSLexerAdapter() {
        super(new PSLanguageLexer((Reader) null));
    }
}
