package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSObjectBinder;

public class PSObjectBinderImpl extends PSPsiElement implements PSObjectBinder {

    public PSObjectBinderImpl(final ASTNode node) {
        super(node);
    }

}
