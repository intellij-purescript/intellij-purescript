package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.psi.impl.PSProperNameImpl;

public interface PSDataDeclaration extends PsiElement {

    PSProperNameImpl getProperName();

}
