package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSLet;

public class PSLetImpl extends PSPsiElement implements PSLet {

    public PSLetImpl(final ASTNode node) {
        super(node);
    }

}
