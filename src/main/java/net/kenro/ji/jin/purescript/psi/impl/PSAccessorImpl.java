package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSAccessor;

public class PSAccessorImpl extends PSPsiElement implements PSAccessor {

    public PSAccessorImpl(final ASTNode node) {
        super(node);
    }

}
