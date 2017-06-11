package net.kenro.ji.jin.purescript.psi.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;

public interface PSReference extends PsiReference {

    PSReference referenceInAncestor(PsiElement ancestor);

    PsiElement getReferencingElement();

    PSReferenceTarget getTarget();
}
