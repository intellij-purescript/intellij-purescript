package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSClassName;

public class PSClassNameImpl extends PSPsiElement implements PSClassName {

    public PSClassNameImpl(final ASTNode node) {
        super(node);
    }

}
