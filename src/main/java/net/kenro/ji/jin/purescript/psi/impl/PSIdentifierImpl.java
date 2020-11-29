package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import net.kenro.ji.jin.purescript.file.PSFile;
import org.jetbrains.annotations.Nullable;

public class PSIdentifierImpl extends PSPsiElement {

    public PSIdentifierImpl(final ASTNode node){
        super(node);
    }

    @Override
    public String getName() {
        return getText().trim();
    }

    @Override
    public PsiReference getReference() {
        return new PsiReferenceBase<>(this, TextRange.allOf(this.getText())) {
            @Override
            public @Nullable
            PsiElement resolve() {
                final String name = myElement.getName();
                final PSFile containingFile = (PSFile) getContainingFile();
                return containingFile
                    .getTopLevelValueDeclarations()
                    .get(name);
            }
        };
    }
}
