package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSGuard;

public class PSGuardImpl extends PSPsiElement implements PSGuard {

    public PSGuardImpl(final ASTNode node) {
        super(node);
    }

}
