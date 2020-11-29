package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSNumericLiteral;

public class PSNumericLiteralImpl extends PSPsiElement implements PSNumericLiteral {

    public PSNumericLiteralImpl(final ASTNode node) {
        super(node);
    }

}
