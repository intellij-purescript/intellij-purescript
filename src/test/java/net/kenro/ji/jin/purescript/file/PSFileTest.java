package net.kenro.ji.jin.purescript.file;

import net.kenro.ji.jin.purescript.parser.PSLanguageParserTestBase;
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl;

import java.util.Map;

public class PSFileTest extends PSLanguageParserTestBase {

    public void testFindTopLevelValueDeclarationWithName() {
        final PSFile file = (PSFile) createFile(
            "Main.purs",
            "module Main where\n" +
                "x = 1\n" +
                "y = 2"
        );
        final Map<String, PSValueDeclarationImpl> valueDeclarations =
            file.getTopLevelValueDeclarations();
        assertSize(2, valueDeclarations.keySet());
        assertNotNull(valueDeclarations.get("x"));
        assertNotNull(valueDeclarations.get("y"));
    }

}