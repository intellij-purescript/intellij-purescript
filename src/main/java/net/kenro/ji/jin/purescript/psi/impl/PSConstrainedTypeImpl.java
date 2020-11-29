package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSConstrainedType;

public class PSConstrainedTypeImpl extends PSPsiElement implements PSConstrainedType {

    public PSConstrainedTypeImpl(final ASTNode node) {
        super(node);
    }

}
