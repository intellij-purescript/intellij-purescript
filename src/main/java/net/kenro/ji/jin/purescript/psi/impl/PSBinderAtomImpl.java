package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSBinderAtom;

public class PSBinderAtomImpl extends PSPsiElement implements PSBinderAtom {

    public PSBinderAtomImpl(final ASTNode node) {
        super(node);
    }

}
