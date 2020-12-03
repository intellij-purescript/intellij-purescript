package net.kenro.ji.jin.purescript;


import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.usageView.UsageInfo;
import net.kenro.ji.jin.purescript.file.PSFile;
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl;
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class LightFindUsageTest extends BasePlatformTestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/testData";
    }

    public void testFindUsageTopLevel() {
        final PSFile main = (PSFile) myFixture.configureByFile("Main.purs");
        final PSValueDeclarationImpl fn = main
            .getTopLevelValueDeclarations()
            .get("fn");
        final Collection<UsageInfo> usages = myFixture.findUsages(fn);
        assertNotEmpty(usages);
    }
    public void testFindUsageSimpleParameter() {
        final PSFile main = (PSFile) myFixture.configureByFile("Main.purs");
        final PSValueDeclarationImpl fn = main
            .getTopLevelValueDeclarations()
            .get("fn");
        final PSIdentifierImpl x = fn
            .getDeclaredIdentifiersInParameterList()
            .get("x");
        final Collection<UsageInfo> usages = myFixture.findUsages(x);
        assertNotEmpty(usages);
    }
}
