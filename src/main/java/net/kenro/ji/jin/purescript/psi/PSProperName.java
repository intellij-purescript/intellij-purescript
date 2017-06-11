package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;

public interface PSProperName extends PSNamedElement {

    String getName();

    PsiElement setName(String newName);

    PsiElement getNameIdentifier();
}
