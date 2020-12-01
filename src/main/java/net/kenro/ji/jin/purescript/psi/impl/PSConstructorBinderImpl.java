package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class PSConstructorBinderImpl extends PSPsiElement implements ContainsIdentifier {

    public PSConstructorBinderImpl(final ASTNode node) {
        super(node);
    }

    @Override
    public Map<String, PSIdentifierImpl> getIdentifiers() {
        return Arrays
            .stream(this.findChildrenByClass(ContainsIdentifier.class))
            .skip(1)
            .map(ContainsIdentifier::getIdentifiers)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
