package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSTypeDeclaration;

public class PSTypeDeclarationImpl extends PSPsiElement implements PSTypeDeclaration {

    public PSTypeDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
