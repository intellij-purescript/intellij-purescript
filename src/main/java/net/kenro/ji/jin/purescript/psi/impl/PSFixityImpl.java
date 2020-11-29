package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSFixity;

public class PSFixityImpl extends PSPsiElement implements PSFixity {

    public PSFixityImpl(final ASTNode node) {
        super(node);
    }

}
