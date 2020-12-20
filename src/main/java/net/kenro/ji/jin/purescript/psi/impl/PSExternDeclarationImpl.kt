package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSExternDeclaration;

public class PSExternDeclarationImpl extends PSPsiElement implements PSExternDeclaration {

    public PSExternDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
