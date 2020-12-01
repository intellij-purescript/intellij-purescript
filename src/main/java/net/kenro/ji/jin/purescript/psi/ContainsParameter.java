package net.kenro.ji.jin.purescript.psi;

import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;

import java.util.Map;

public interface ContainsParameter {

    Map<String, PSIdentifierImpl> getParameters();
}
