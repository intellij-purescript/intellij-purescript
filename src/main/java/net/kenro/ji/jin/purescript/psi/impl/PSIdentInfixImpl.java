package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSIdentInfix;

public class PSIdentInfixImpl extends PSPsiElement implements PSIdentInfix {

    public PSIdentInfixImpl(final ASTNode node) {
        super(node);
    }

}
