package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PSValueDeclarationImpl extends PSPsiElement implements PsiNameIdentifierOwner {

    public PSValueDeclarationImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return this.findChildByClass(PSIdentifierImpl.class).getName();
    }

    @Override
    public PsiElement setName(@NotNull final String name) throws IncorrectOperationException {
        return null;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return this.findChildByClass(PSIdentifierImpl.class);
    }

    public Map<String, PSIdentifierImpl> getParameters() {
        return Arrays
            .stream(this.getChildren())
            .skip(1)
            .filter(psi -> psi instanceof  PSIdentifierImpl)
            .map(psi -> (PSIdentifierImpl) psi)
            .collect(Collectors.toMap(
                psIdentifier -> psIdentifier.getName(),
                psIdentifier -> psIdentifier
            ));
    }

}
