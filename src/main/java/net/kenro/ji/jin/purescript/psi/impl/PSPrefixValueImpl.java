package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSPrefixValue;

public class PSPrefixValueImpl extends PSPsiElement implements PSPrefixValue {

    public PSPrefixValueImpl(final ASTNode node) {
        super(node);
    }

}
