package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PSImportDeclarationImpl extends PSPsiElement {

    public PSImportDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public String getModuleName() {
        return null;
    }

    @Nullable
    public List<String> getExposingClause() {
        return null;
    }
}
