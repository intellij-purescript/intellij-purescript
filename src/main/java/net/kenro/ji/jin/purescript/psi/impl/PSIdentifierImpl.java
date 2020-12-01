package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PSIdentifierImpl extends PSPsiElement implements ContainsIdentifier {

    public PSIdentifierImpl(final ASTNode node){
        super(node);
    }

    @Override
    @NotNull
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

    @Override
    public Map<String, PSIdentifierImpl> getIdentifiers() {
        return Map.of(this.getName(), this);
    }
}
