package net.kenro.ji.jin.purescript.parser;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class ParsecRef extends Parsec {
    private Parsec ref;

    public void setRef(@NotNull final Parsec ref) {
        this.ref = ref;
    }

    @NotNull
    @Override
    public ParserInfo parse(@NotNull final ParserContext context) {
        return ref.parse(context);
    }

    @NotNull
    @Override
    public String calcName() {
        return ref.getName();
    }

    @NotNull
    @Override
    protected HashSet<String> calcExpectedName() {
        return ref.getExpectedName();
    }

    @Override
    public boolean canStartWith(@NotNull final IElementType type) {
        return ref.canStartWith(type);
    }

    @Override
    public boolean calcCanBeEmpty() {
        return ref.canBeEmpty();
    }
}
