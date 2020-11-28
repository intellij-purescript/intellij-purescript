package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSValueDeclaration;

public class PSValueDeclarationImpl extends PSPsiElement implements PSValueDeclaration {

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
