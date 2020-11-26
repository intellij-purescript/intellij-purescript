package net.kenro.ji.jin.purescript.features;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import net.kenro.ji.jin.purescript.psi.PSTokens;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PSPairedBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[] {
            new BracePair(PSTokens.LCURLY, PSTokens.RCURLY, true),
            new BracePair(PSTokens.LBRACK, PSTokens.RBRACK, true),
            new BracePair(PSTokens.LPAREN, PSTokens.RPAREN, false)
    };

    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType contextType) {
        return contextType == null
                || contextType == PSTokens.WS;
    }

    @Override
    public int getCodeConstructStart(final PsiFile file, final int openingBraceOffset) {
        return openingBraceOffset;
    }
}
