package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;

public class PSBinderAtomImpl extends PSPsiElement implements ContainsIdentifier {

    public PSBinderAtomImpl(final ASTNode node) {
        super(node);
    }

}
