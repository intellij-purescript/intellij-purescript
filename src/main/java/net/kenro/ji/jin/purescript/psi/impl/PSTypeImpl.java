package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSType;

public class PSTypeImpl extends PSPsiElement implements PSType {

    public PSTypeImpl(final ASTNode node) {
        super(node);
    }

}
