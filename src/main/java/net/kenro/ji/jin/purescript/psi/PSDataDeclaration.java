package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface PSDataDeclaration extends PsiElement {

    @NotNull
    PSProperName getProperName();

}
