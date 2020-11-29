package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSGenericIdentifier;

public class PSGenericIdentifierImpl extends PSPsiElement implements PSGenericIdentifier {

    public PSGenericIdentifierImpl(final ASTNode node) {
        super(node);
    }

}
