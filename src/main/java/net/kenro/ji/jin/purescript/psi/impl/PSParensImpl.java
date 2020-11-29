package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSParens;

public class PSParensImpl extends PSPsiElement implements PSParens {

    public PSParensImpl(final ASTNode node) {
        super(node);
    }

}
