package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSStringBinder;

public class PSStringBinderImpl extends PSPsiElement implements PSStringBinder {

    public PSStringBinderImpl(final ASTNode node) {
        super(node);
    }

}
