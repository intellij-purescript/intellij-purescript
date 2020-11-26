package net.kenro.ji.jin.purescript.psi.cst;

import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.injected.StringLiteralEscaper;

public class PSStringLiteralEscaper extends StringLiteralEscaper<PsiLanguageInjectionHost> {
    public PSStringLiteralEscaper(final PsiLanguageInjectionHost host) {
        super(host);
    }
}
