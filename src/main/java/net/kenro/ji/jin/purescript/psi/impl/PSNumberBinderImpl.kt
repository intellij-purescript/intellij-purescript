package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSNumberBinder;

public class PSNumberBinderImpl extends PSPsiElement implements PSNumberBinder {

    public PSNumberBinderImpl(final ASTNode node) {
        super(node);
    }

}
