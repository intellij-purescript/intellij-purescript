package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSBooleanBinder;

public class PSBooleanBinderImpl extends PSPsiElement implements PSBooleanBinder {

    public PSBooleanBinderImpl(final ASTNode node) {
        super(node);
    }

}
