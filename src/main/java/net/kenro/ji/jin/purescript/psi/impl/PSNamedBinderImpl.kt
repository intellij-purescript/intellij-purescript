package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSNamedBinder;

public class PSNamedBinderImpl extends PSPsiElement implements PSNamedBinder {

    public PSNamedBinderImpl(final ASTNode node) {
        super(node);
    }

}
