package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.util.IncorrectOperationException;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public Map<String, PSIdentifierImpl> getDeclaredIdentifiersInParameterList() {
        final Stream<Map<String, PSIdentifierImpl>> identifiers = Arrays
            .stream(this.findChildrenByClass(PSIdentifierImpl.class))
            .skip(1)
            .map(ContainsIdentifier::getIdentifiers);
        return
            Stream.concat(
                identifiers,
                Arrays
                    .stream(this.findChildrenByClass(DeclaresIdentifiers.class))
                    .map(DeclaresIdentifiers::getDeclaredIdentifiers)
            ).map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
