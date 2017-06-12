package net.kenro.ji.jin.purescript.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.psi.PSProperName;
import net.kenro.ji.jin.purescript.psi.scope.PSScope;
import net.kenro.ji.jin.purescript.util.Function3;
import org.jetbrains.annotations.Nullable;

public class PSTypeReference extends PSReferenceBase<PSProperName> {
    public PSTypeReference(PSProperName element) {
        super(element);
    }

    private PSTypeReference(PsiElement element, PSProperName referencingElement, TextRange rangeInElement) {
        super(element, referencingElement, rangeInElement);
    }

    @Override
    protected Function3<PsiElement, PSProperName, TextRange, PSReference> constructor() {
        return PSTypeReference::new;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiFile file = this.myElement.getContainingFile();
        return PSScope.typesFor((PSFile) file)
                .filter(this::theSameNameOrEmpty)
                .findFirst()
                .map(o -> o.orElse(null))
                .orElse(null);
    }
}
