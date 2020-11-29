package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSVarBinder;

public class PSVarBinderImpl extends PSPsiElement implements PSVarBinder {

    public PSVarBinderImpl(final ASTNode node) {
        super(node);
    }

}
