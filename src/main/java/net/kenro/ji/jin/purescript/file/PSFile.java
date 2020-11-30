package net.kenro.ji.jin.purescript.file;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import net.kenro.ji.jin.purescript.PSLanguage;
import net.kenro.ji.jin.purescript.psi.impl.PSProgramImpl;
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

public class PSFile extends PsiFileBase {
    public PSFile(@NotNull final FileViewProvider viewProvider) {
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
    public Icon getIcon(final int flags) {
        return super.getIcon(flags);
    }

    public Map<String, PSValueDeclarationImpl> getTopLevelValueDeclarations() {
        return this
            .getProgram()
            .getModule()
            .getTopLevelValueDeclarations()
        ;
    }

    private PSProgramImpl getProgram() {
        return this.findChildByClass(PSProgramImpl.class);
    }
}
