package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class PSVarBinderImpl extends PSPsiElement implements DeclaresIdentifiers {

    public PSVarBinderImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public Map<String, PSIdentifierImpl> getDeclaredIdentifiers() {
        return Arrays
            .stream(this.findChildrenByClass(ContainsIdentifier.class))
            .map(ContainsIdentifier::getIdentifiers)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
