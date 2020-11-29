package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSConstructor;

public class PSConstructorImpl extends PSPsiElement implements PSConstructor {

    public PSConstructorImpl(final ASTNode node) {
        super(node);
    }

}
