package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSDoNotationLet;

public class PSDoNotationLetImpl extends PSPsiElement implements PSDoNotationLet {

    public PSDoNotationLetImpl(final ASTNode node) {
        super(node);
    }

}
