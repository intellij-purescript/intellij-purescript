package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSRowKind;

public class PSRowKindImpl extends PSPsiElement implements PSRowKind {

    public PSRowKindImpl(final ASTNode node) {
        super(node);
    }

}
