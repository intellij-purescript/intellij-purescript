package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSFunKind;

public class PSFunKindImpl extends PSPsiElement implements PSFunKind {

    public PSFunKindImpl(final ASTNode node) {
        super(node);
    }

}
