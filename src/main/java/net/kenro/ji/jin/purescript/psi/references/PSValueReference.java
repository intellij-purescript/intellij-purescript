package net.kenro.ji.jin.purescript.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.PSIdentifier;
import net.kenro.ji.jin.purescript.psi.scope.PSScope;
import net.kenro.ji.jin.purescript.util.Function3;
import org.jetbrains.annotations.Nullable;

public class PSValueReference extends PSReferenceBase<PSIdentifier> {

    public PSValueReference(PSIdentifier element) {
        super(element);
    }

    private PSValueReference(PsiElement element, PSIdentifier referencingElement, TextRange rangeInElement) {
        super(element, referencingElement, rangeInElement);
    }

    @Override
    protected Function3<PsiElement, PSIdentifier, TextRange, PSReference> constructor() {
        return PSValueReference::new;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return PSScope.scopeFor(this.referencingElement)
                .filter(this::theSameNameOrEmpty)
                .findFirst()
                .map(o -> o.orElse(null))
                .orElse(null);
    }
}
