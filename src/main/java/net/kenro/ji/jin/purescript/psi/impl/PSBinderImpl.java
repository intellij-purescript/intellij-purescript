package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;


public class PSBinderImpl extends PSPsiElement implements ContainsIdentifier {

    public PSBinderImpl(final ASTNode node) {
        super(node);
    }

}
