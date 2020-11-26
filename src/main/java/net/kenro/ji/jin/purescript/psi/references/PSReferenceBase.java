package net.kenro.ji.jin.purescript.psi.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import net.kenro.ji.jin.purescript.util.Function3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

abstract class PSReferenceBase <T extends PsiElement> extends PsiReferenceBase<PsiElement> implements PSReference {

    final T referencingElement;

    PSReferenceBase(final T element) {
        this(element, element, new TextRange(0, element.getTextLength()));
    }

    PSReferenceBase(final PsiElement element, final T referencingElement, final TextRange rangeInElement) {
        super(element, rangeInElement);
        this.referencingElement = referencingElement;
    }

    public PsiElement getReferencingElement() {
        return this.referencingElement;
    }

    @Override
    public PSReference referenceInAncestor(final PsiElement ancestor) {
        final int diff = this.myElement.getTextOffset() - ancestor.getTextOffset();
        return constructor().apply(ancestor, this.referencingElement, this.getRangeInElement().shiftRight(diff));
    }

    protected abstract Function3<PsiElement, T, TextRange, PSReference> constructor();

    @Override
    public PSReferenceTarget getTarget() {
        return PSReferenceTarget.SYMBOL;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    boolean theSameName(@NotNull final PsiElement element) {
        return element.getText().equals(this.referencingElement.getText());
    }

    boolean theSameNameOrEmpty(final Optional<T> optionalElem) {
        return optionalElem.map(this::theSameName)
                .orElse(true);
    }
}
