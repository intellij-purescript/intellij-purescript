package net.kenro.ji.jin.purescript.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class PSVisitor extends PsiElementVisitor {

    public void visitPsiElement(@NotNull PsiElement o) {
        visitElement(o);
    }

    public void visitNamedElement(@NotNull PSNamedElement o) {
        visitPsiElement(o);
    }

    public void visitPSProperName(@NotNull PSProperName o) {
        visitNamedElement(o);
    }

    public void visitPSIdentifier(@NotNull PSIdentifier o) {
        visitNamedElement(o);
    }



}
