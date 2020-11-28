package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;

public class PSValueDeclarationImpl extends PSPsiElement {

    public PSValueDeclarationImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final PSIdentifierImpl identifier =
            this.findChildByClass(PSIdentifierImpl.class);
        return identifier.getName();
    }
}
