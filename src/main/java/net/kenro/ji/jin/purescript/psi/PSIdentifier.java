package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;

public interface PSIdentifier extends PSNamedElement {

    String getName();

    PsiElement setName(String newName);

    PsiElement getNameIdentifier();
}
