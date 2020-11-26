package net.kenro.ji.jin.purescript.psi.cst;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ReflectionCache;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PSElement extends CompositePsiElement {
    protected PSElement(@NotNull final IElementType type) {
        super(type);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public <T extends PSElement> T[] findChildren(final Class<T> type) {
        final ArrayList<T> result = new ArrayList<T>();
        for (final PsiElement psiElement : this.getChildren()) {
            if (ReflectionCache.isInstance(psiElement, type)) {
                result.add((T) psiElement);
            }
        }

        return result.toArray((T[]) Array.newInstance(type, result.size()));
    }
}
