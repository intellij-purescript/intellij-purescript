package net.kenro.ji.jin.purescript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiUtil;
import net.kenro.ji.jin.purescript.psi.PSElements;
import net.kenro.ji.jin.purescript.psi.PSImportDeclaration;
import net.kenro.ji.jin.purescript.psi.PSProperName;
import net.kenro.ji.jin.purescript.psi.PSVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class PSImportDeclarationImpl extends PSPsiElement implements PSImportDeclaration {

    public PSImportDeclarationImpl(ASTNode node) {
        super(node);




//        List<PSProperName> names = Arrays.asList(Arrays.stream(node.getPsi().getChildren())
//                .filter(e -> ).toArray());

        System.out.println("  IN THE IMPORT DECLARATION IMPL  ");
    }

    public void accept(@NotNull PSVisitor visitor) {
        visitor.visitPSImportDeclaration(this);
    }

    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof PSVisitor) accept((PSVisitor) visitor);
        else super.accept(visitor);
    }


    public String getModuleName() {
        return null;
    }


    @Override
    @Nullable
    public List<String> getExposingClause() {
        return null;
    }
}
