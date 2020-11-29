package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSModule;

public class PSModuleImpl extends PSPsiElement implements PSModule {

    public PSModuleImpl(final ASTNode node) {
        super(node);
    }

}
