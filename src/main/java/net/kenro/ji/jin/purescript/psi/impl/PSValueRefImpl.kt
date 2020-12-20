package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSValueRef;

public class PSValueRefImpl extends PSPsiElement implements PSValueRef {

    public PSValueRefImpl(final ASTNode node) {
        super(node);
    }

}
