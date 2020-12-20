package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSRow;

public class PSRowImpl extends PSPsiElement implements PSRow {

    public PSRowImpl(final ASTNode node) {
        super(node);
    }

}
