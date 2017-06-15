package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PSDataDeclaration extends PsiElement {

    @NotNull
    PSProperName getProperName();

}
