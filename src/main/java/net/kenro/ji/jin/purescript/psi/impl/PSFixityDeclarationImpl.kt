package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSFixityDeclaration;

public class PSFixityDeclarationImpl extends PSPsiElement implements PSFixityDeclaration {

    public PSFixityDeclarationImpl(final ASTNode node) {
        super(node);
    }

}
