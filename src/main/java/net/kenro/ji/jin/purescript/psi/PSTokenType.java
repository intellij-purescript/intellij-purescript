package net.kenro.ji.jin.purescript.psi;

import com.intellij.psi.tree.IElementType;
import net.kenro.ji.jin.purescript.PSLanguage;
import org.jetbrains.annotations.*;

public class PSTokenType extends IElementType {
    public PSTokenType(@NotNull @NonNls String debugName) {
        super(debugName, PSLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "PSTokenType." + super.toString();
    }
}
