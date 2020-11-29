package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSNullBinder;

public class PSNullBinderImpl extends PSPsiElement implements PSNullBinder {

    public PSNullBinderImpl(final ASTNode node) {
        super(node);
    }

}
