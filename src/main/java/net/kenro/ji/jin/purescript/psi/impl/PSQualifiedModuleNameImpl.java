package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSQualifiedModuleName;

public class PSQualifiedModuleNameImpl extends PSPsiElement implements PSQualifiedModuleName {

    public PSQualifiedModuleNameImpl(final ASTNode node) {
        super(node);
    }

}
