package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;

public class PSIdentifierImpl extends PSPsiElement {

    public PSIdentifierImpl(final ASTNode node){
        super(node);
    }

    @Override
    public String getName() {
        return getText().trim();
    }

}
