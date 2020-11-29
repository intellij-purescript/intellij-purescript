package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSTypeHole;

public class PSTypeHoleImpl extends PSPsiElement implements PSTypeHole {

    public PSTypeHoleImpl(final ASTNode node) {
        super(node);
    }

}
