package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;

public class PSProperNameImpl extends PSPsiElement {

    public PSProperNameImpl(final ASTNode node){
        super(node);
    }

    public String getName() {
        return PSPsiImplUtil.getName(this);
    }

}
