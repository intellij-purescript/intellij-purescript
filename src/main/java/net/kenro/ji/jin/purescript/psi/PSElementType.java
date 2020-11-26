package net.kenro.ji.jin.purescript.psi;


import com.intellij.psi.tree.IElementType;
import net.kenro.ji.jin.purescript.PSLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PSElementType extends IElementType {
    public PSElementType(@NotNull @NonNls final String debugName) {
        super(debugName, PSLanguage.INSTANCE);
    }
}