package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSDataDeclaration;

public class PSDataDeclarationImpl extends PSPsiElement implements PSDataDeclaration {

    public PSDataDeclarationImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public PSProperNameImpl getProperName() {
        return null;
    }
}
