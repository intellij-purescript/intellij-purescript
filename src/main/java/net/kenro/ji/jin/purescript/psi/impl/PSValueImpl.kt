package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSValue;

public class PSValueImpl extends PSPsiElement implements PSValue {

    public PSValueImpl(final ASTNode node) {
        super(node);
    }

}
