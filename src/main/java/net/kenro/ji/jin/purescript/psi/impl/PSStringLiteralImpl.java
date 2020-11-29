package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSStringLiteral;

public class PSStringLiteralImpl extends PSPsiElement implements PSStringLiteral {

    public PSStringLiteralImpl(final ASTNode node) {
        super(node);
    }

}
