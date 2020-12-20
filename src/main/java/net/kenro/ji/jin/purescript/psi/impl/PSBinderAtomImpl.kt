package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers;

public class PSBinderAtomImpl extends PSPsiElement implements DeclaresIdentifiers {

    public PSBinderAtomImpl(final ASTNode node) {
        super(node);
    }

}
