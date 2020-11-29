package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSBooleanLiteral;

public class PSBooleanLiteralImpl extends PSPsiElement implements PSBooleanLiteral {

    public PSBooleanLiteralImpl(final ASTNode node) {
        super(node);
    }

}
