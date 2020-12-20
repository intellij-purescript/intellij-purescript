package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSVar;

public class PSVarImpl extends PSPsiElement implements PSVar {

    public PSVarImpl(final ASTNode node) {
        super(node);
    }

}
