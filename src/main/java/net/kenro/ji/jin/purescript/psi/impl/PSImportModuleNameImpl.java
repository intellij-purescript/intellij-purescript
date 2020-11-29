package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import net.kenro.ji.jin.purescript.psi.PSImportModuleName;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;

public class PSImportModuleNameImpl extends PSPsiElement implements PSImportModuleName {

    public PSImportModuleNameImpl(final ASTNode node) {
        super(node);
    }

}
