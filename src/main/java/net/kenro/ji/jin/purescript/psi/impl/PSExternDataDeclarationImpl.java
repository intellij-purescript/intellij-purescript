package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSExternDataDeclaration;

public class PSExternDataDeclarationImpl extends PSPsiElement implements PSExternDataDeclaration {

    public PSExternDataDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
