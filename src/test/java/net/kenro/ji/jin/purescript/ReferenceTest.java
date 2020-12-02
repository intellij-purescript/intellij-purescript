package net.kenro.ji.jin.purescript;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
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

    public void testFindParametersForValueDeclaration() {
        final PSFile file = (PSFile) createFile(
            "Main.purs",
            "module Main where\n" +
                "fn x (z) (Just n) = x + y\n" +
                "y = 2"
        );
        final Map<String, PSValueDeclarationImpl> valueDeclarations =
            file.getTopLevelValueDeclarations();
        final PSValueDeclarationImpl fn = valueDeclarations.get("fn");
        final Map<String, PSIdentifierImpl> parameterDeclarations =
            fn.getDeclaredIdentifiersInParameterList();
        assertContainsElements(parameterDeclarations.keySet(), "x", "z", "n");
        assertDoesntContain(parameterDeclarations.keySet(), "fn", "y", "Just");
        final PSIdentifierImpl x = parameterDeclarations.get("x");
        assertEquals("x", x.getName() );
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
        final PsiReference reference = psIdentifier.getReference();
        assertTrue(
            "identifier reference should include the whole name in its range",
            reference.getRangeInElement().contains(0)
        );
        final PsiElement resolved = reference.resolve();
        assertInstanceOf(resolved, PSValueDeclarationImpl.class);
        assertEquals("x", ((PSValueDeclarationImpl) resolved).getName());
    }
}