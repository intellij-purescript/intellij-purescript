package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSTypeAnnotationName;

public class PSTypeAnnotationNameImpl extends PSPsiElement implements PSTypeAnnotationName {

    public PSTypeAnnotationNameImpl(final ASTNode node) {
        super(node);
    }

}
