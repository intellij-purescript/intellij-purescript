package net.kenro.ji.jin.purescript.psi.references;

public enum PSReferenceTarget {
    SYMBOL("symbol"),
    MODULE("module");

    private final String displayName;

    PSReferenceTarget(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
