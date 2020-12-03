package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

// PsiNamedElement is only here so that the editor can find the the
// Identifier when it is in a parameter
public class PSIdentifierImpl extends PSPsiElement implements ContainsIdentifier, PsiNamedElement {

    public PSIdentifierImpl(final ASTNode node){
        super(node);
    }

    @Override
    @NotNull
    public String getName() {
        return getText().trim();
    }

    @Override
    public PsiElement setName(@NotNull final String name) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiReference getReference() {
        return new PsiReferenceBase<>(this, TextRange.allOf(this.getName())) {
            @Override
            public @Nullable
            PsiElement resolve() {
                final PsiElement resolved = getParameterReference()
                    .orElseGet(this::getTopLevelValueDeclarationReference);
                return resolved;
            }

            private PSValueDeclarationImpl getTopLevelValueDeclarationReference() {
                final PSFile containingFile = (PSFile) getContainingFile();
                return containingFile
                .getTopLevelValueDeclarations()
                .get(myElement.getName());
            }

            @NotNull
            private Optional<PsiElement> getParameterReference() {
                return getContainingValueDeclaration()
                    .flatMap(psi -> Optional.ofNullable(psi
                        .getDeclaredIdentifiersInParameterList()
                        .get(myElement.getName()))
                    )
                    .map(psi -> psi);
            }

            @NotNull
            private Optional<PSValueDeclarationImpl> getContainingValueDeclaration() {
                return Stream
                    .iterate(myElement, PsiElement::getParent)
                    .takeWhile(Objects::nonNull)
                    .filter(psi -> psi instanceof PSValueDeclarationImpl)
                    .map(psi -> (PSValueDeclarationImpl) psi)
                    .findFirst();
            }
        };
    }

    @Override
    public Map<String, PSIdentifierImpl> getIdentifiers() {
        return Map.of(this.getName(), this);
    }
}
