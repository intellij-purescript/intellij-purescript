package net.kenro.ji.jin.purescript.psi.cst;

import net.kenro.ji.jin.purescript.psi.PSElements;
import org.jetbrains.annotations.NotNull;

public class Program extends PSElement {
    protected Program() {
        super(PSElements.Program);
    }

    @NotNull
    public Module[] getModules() {
        return this.findChildren(Module.class);
    }
}
