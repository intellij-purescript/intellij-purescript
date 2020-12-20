package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSLocalIdentifier;

public class PSLocalIdentifierImpl extends PSPsiElement implements PSLocalIdentifier {

    public PSLocalIdentifierImpl(final ASTNode node) {
        super(node);
    }

}
