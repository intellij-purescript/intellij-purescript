package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSConstructorBinder;

public class PSConstructorBinderImpl extends PSPsiElement implements PSConstructorBinder {

    public PSConstructorBinderImpl(final ASTNode node) {
        super(node);
    }

}
