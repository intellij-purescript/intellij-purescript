package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSModuleName;

public class PSModuleNameImpl extends PSPsiElement implements PSModuleName {

    public PSModuleNameImpl(final ASTNode node) {
        super(node);
    }

}
