package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSObjectBinderField;

public class PSObjectBinderFieldImpl extends PSPsiElement implements PSObjectBinderField {

    public PSObjectBinderFieldImpl(final ASTNode node) {
        super(node);
    }

}
