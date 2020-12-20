package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSForAll;

public class PSForAllImpl extends PSPsiElement implements PSForAll {

    public PSForAllImpl(final ASTNode node) {
        super(node);
    }

}
