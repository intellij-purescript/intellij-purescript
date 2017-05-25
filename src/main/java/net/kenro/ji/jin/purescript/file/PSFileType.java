package net.kenro.ji.jin.purescript.file;

import com.intellij.openapi.fileTypes.LanguageFileType;
import net.kenro.ji.jin.purescript.icons.PSIcons;
import net.kenro.ji.jin.purescript.PSLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PSFileType extends LanguageFileType {

    public static final PSFileType INSTANCE = new PSFileType();
    public static final String DEFAULT_EXTENSION = "purs";

    private PSFileType() {
        super(PSLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Purescript file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Purescript file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return PSIcons.FILE;
    }
}
