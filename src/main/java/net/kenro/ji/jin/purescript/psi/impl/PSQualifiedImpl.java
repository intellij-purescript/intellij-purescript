package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSQualified;

public class PSQualifiedImpl extends PSPsiElement implements PSQualified {

    public PSQualifiedImpl(final ASTNode node) {
        super(node);
    }

}
