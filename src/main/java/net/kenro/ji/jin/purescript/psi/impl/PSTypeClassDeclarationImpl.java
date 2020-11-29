package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSTypeClassDeclaration;

public class PSTypeClassDeclarationImpl extends PSPsiElement implements PSTypeClassDeclaration {

    public PSTypeClassDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
