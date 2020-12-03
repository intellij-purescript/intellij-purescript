package net.kenro.ji.jin.purescript.features;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PSFindUsageProvider implements FindUsagesProvider {
    @Override
    public boolean canFindUsagesFor(@NotNull final PsiElement psiElement) {
        return psiElement instanceof PSValueDeclarationImpl
            || psiElement instanceof PSIdentifierImpl;
    }

    @Override
    public @Nullable String getHelpId(@NotNull final PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull final PsiElement element) {
        if (element instanceof PSValueDeclarationImpl) {
            return "value";
        } else if (element instanceof PSIdentifierImpl) {
            return "parameter";
        }
        return "unknown";
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull final PsiElement element) {
        if (element instanceof PsiNamedElement) {
            final String name = ((PsiNamedElement) element).getName();
            if (name != null) {
                return name;
            }
        }
        return "";
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull final PsiElement element, final boolean useFullName) {
        if (element instanceof PsiNamedElement) {
            final String name = ((PsiNamedElement) element).getName();
            if (name != null) {
                return name;
            }
        }
        return "";
    }
}
