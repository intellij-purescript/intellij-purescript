package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSCase;

public class PSCaseImpl extends PSPsiElement implements PSCase {

    public PSCaseImpl(final ASTNode node) {
        super(node);
    }

}
