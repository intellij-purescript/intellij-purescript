package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSUnaryMinus;

public class PSUnaryMinusImpl extends PSPsiElement implements PSUnaryMinus {

    public PSUnaryMinusImpl(final ASTNode node) {
        super(node);
    }

}
