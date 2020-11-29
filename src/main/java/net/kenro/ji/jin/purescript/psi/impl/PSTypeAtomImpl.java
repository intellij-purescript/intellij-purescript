package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSTypeAtom;

public class PSTypeAtomImpl extends PSPsiElement implements PSTypeAtom {

    public PSTypeAtomImpl(final ASTNode node) {
        super(node);
    }

}
