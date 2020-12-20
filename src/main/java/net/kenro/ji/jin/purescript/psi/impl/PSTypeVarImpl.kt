package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSTypeVar;

public class PSTypeVarImpl extends PSPsiElement implements PSTypeVar {

    public PSTypeVarImpl(final ASTNode node) {
        super(node);
    }

}
