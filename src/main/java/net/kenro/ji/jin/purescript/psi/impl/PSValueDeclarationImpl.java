package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
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
            .stream(this.findChildrenByClass(ContainsIdentifier.class))
            .skip(1)
            .map(ContainsIdentifier::getIdentifiers)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
