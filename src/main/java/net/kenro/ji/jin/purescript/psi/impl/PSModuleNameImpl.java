package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSModuleName;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSModuleNameImpl extends PSPsiElement implements PSModuleName {

    public PSModuleNameImpl(final ASTNode node) {
        super(node);
    }

}
