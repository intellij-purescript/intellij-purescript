package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

public class PSDataDeclarationImpl extends PSPsiElement {

    public PSDataDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public PSProperNameImpl getProperName() {
        return null;
    }
}
