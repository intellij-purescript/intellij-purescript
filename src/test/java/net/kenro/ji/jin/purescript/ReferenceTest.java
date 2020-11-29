package net.kenro.ji.jin.purescript;

import com.intellij.psi.PsiElement;
import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.parser.PSLanguageParserTestBase;
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl;

import java.util.Map;

public class ReferenceTest extends PSLanguageParserTestBase {

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

    public void testIdentifierCanResolveToToplevelValueDeclaration() {
        final PSFile file = (PSFile) createFile(
            "Main.purs",
            "module Main where\n" +
                "x = 1\n" +
                "y = x"
        );
        final PSIdentifierImpl psIdentifier =
            (PSIdentifierImpl) file.findElementAt(28).getParent();
        final PsiElement resolved = psIdentifier.getReference().resolve();
        assertInstanceOf(resolved, PSValueDeclarationImpl.class);
        assertEquals("x", ((PSValueDeclarationImpl) resolved).getName());
    }
}