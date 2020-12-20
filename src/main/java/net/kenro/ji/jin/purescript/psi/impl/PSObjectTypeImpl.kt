package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSObjectType;

public class PSObjectTypeImpl extends PSPsiElement implements PSObjectType {

    public PSObjectTypeImpl(final ASTNode node) {
        super(node);
    }

}
