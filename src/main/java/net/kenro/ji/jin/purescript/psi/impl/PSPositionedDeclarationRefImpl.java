package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSPositionedDeclarationRef;

public class PSPositionedDeclarationRefImpl extends PSPsiElement implements PSPositionedDeclarationRef {

    public PSPositionedDeclarationRefImpl(final ASTNode node) {
        super(node);
    }

}
