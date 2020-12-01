package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;

public class PSVarBinderImpl extends PSPsiElement implements ContainsIdentifier {

    public PSVarBinderImpl(final ASTNode node) {
        super(node);
    }

}
