package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface PSProperName extends PsiNameIdentifierOwner {

    String getName();

    PsiElement setName(String newName);

    PsiElement getNameIdentifier();
}
