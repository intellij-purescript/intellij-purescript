package net.kenro.ji.jin.purescript.file;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import net.kenro.ji.jin.purescript.PSLanguage;
import net.kenro.ji.jin.purescript.psi.PSImportDeclaration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class PSFile extends PsiFileBase {
    public PSFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, PSLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PSFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Purescript File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }

    public List<PSImportDeclaration> getImportClauses() {
        return new LinkedList<>(PsiTreeUtil.findChildrenOfType(this, PSImportDeclaration.class));
    }
}
