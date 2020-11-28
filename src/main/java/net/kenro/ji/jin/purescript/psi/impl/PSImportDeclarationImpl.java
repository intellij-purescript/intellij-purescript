package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.PSImportDeclaration;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PSImportDeclarationImpl extends PSPsiElement implements PSImportDeclaration {

    public PSImportDeclarationImpl(final ASTNode node) {
        super(node);
    }

    public String getModuleName() {
        return null;
    }

    @Override
    @Nullable
    public List<String> getExposingClause() {
        return null;
    }
}
