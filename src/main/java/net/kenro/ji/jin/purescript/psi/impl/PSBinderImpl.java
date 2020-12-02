package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers;


public class PSBinderImpl extends PSPsiElement implements DeclaresIdentifiers {

    public PSBinderImpl(final ASTNode node) {
        super(node);
    }

}
