package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSQualifiedModuleName;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSQualifiedModuleNameImpl extends PSPsiElement implements PSQualifiedModuleName {

    public PSQualifiedModuleNameImpl(final ASTNode node) {
        super(node);
    }

}
