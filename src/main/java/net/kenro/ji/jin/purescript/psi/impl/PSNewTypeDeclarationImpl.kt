package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSNewTypeDeclaration;

public class PSNewTypeDeclarationImpl extends PSPsiElement implements PSNewTypeDeclaration {

    public PSNewTypeDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
