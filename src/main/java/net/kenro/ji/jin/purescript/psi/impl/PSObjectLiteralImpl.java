package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSObjectLiteral;

public class PSObjectLiteralImpl extends PSPsiElement implements PSObjectLiteral {

    public PSObjectLiteralImpl(final ASTNode node) {
        super(node);
    }

}
