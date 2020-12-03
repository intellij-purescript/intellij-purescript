package net.kenro.ji.jin.purescript.psi;

import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface DeclaresIdentifiers extends PsiElement {

    default Map<String, PSIdentifierImpl> getDeclaredIdentifiers() {
        return Arrays
            .stream(this.getChildren())
            .map(this::getIdentifierFromPSI)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, PSIdentifierImpl> getIdentifierFromPSI(final PsiElement psi) {
        if (psi instanceof DeclaresIdentifiers) {
            final DeclaresIdentifiers containsIdentifier =
                (DeclaresIdentifiers) psi;
            return containsIdentifier.getDeclaredIdentifiers();
        } else {
            return Map.of();
        }
    }
}
