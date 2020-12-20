package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSArrayLiteral;

public class PSArrayLiteralImpl extends PSPsiElement implements PSArrayLiteral {

    public PSArrayLiteralImpl(final ASTNode node) {
        super(node);
    }

}
