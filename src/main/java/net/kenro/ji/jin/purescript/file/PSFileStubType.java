package net.kenro.ji.jin.purescript.file;

import com.intellij.psi.tree.IStubFileElementType;
import net.kenro.ji.jin.purescript.PSLanguage;

public class PSFileStubType extends IStubFileElementType {
    public static final PSFileStubType INSTANCE = new PSFileStubType();

    private PSFileStubType() {
        super(PSLanguage.INSTANCE);
    }
}

