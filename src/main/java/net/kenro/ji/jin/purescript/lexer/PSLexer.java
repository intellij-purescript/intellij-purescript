package net.kenro.ji.jin.purescript.lexer;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import net.kenro.ji.jin.purescript.psi.PSTokens;

import java.io.Reader;

public final class PSLexer extends LookAheadLexer {
    public PSLexer() {
        super(new MergedPureLexer(), 64);
    }

    private static final class MergedPureLexer extends MergingLexerAdapterBase {
        public MergedPureLexer() {
            super(new FlexAdapter(new _PSLexer((Reader) null)));
        }

        private static final MergeFunction mergeFunction = new MergeFunction() {
            @Override
            public IElementType merge(IElementType type, Lexer originalLexer) {
                if (type == PSTokens.STRING) {
                    while (true) {
                        final IElementType tokenType = originalLexer.getTokenType();
                        if (tokenType != PSTokens.STRING && tokenType != PSTokens.STRING_ESCAPED && tokenType != PSTokens.STRING_GAP)
                            break;
                        originalLexer.advance();
                    }
                } else if (type == PSTokens.MLCOMMENT || type == PSTokens.SLCOMMENT || type == PSTokens.WS) {
                    while (true) {
                        type = originalLexer.getTokenType();
                        if (type == PSTokens.MLCOMMENT || type == PSTokens.SLCOMMENT || type == PSTokens.WS) {
                            originalLexer.advance();
                        } else {
                            break;
                        }
                    }
                    type = PSTokens.WS;
                }
                return type;
            }
        };

        @Override
        public MergeFunction getMergeFunction() {
            return mergeFunction;
        }
    }
}
