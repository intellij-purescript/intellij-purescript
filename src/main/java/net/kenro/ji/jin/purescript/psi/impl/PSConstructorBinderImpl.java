package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PSConstructorBinderImpl extends PSPsiElement implements DeclaresIdentifiers {

    public PSConstructorBinderImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public Map<String, PSIdentifierImpl> getDeclaredIdentifiers() {
        final Stream<Map<String, PSIdentifierImpl>> identifiers = Arrays
            .stream(this.findChildrenByClass(PSIdentifierImpl.class))
            .skip(1)
            .map(ContainsIdentifier::getIdentifiers);
        return
            java.util.stream.Stream.concat(
                identifiers,
                Arrays
                    .stream(this.findChildrenByClass(DeclaresIdentifiers.class))
                    .map(DeclaresIdentifiers::getDeclaredIdentifiers)
            ).map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
