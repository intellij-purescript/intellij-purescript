package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSIfThenElse;

public class PSIfThenElseImpl extends PSPsiElement implements PSIfThenElse {

    public PSIfThenElseImpl(final ASTNode node) {
        super(node);
    }

}
