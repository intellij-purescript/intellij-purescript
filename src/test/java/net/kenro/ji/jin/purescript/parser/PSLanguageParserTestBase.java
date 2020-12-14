package net.kenro.ji.jin.purescript.parser;

import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public abstract class PSLanguageParserTestBase extends ParsingTestCase {

    public PSLanguageParserTestBase() {
        super("parser", "purs", new PSParserDefinition());
    }

    @Override
    protected String getTestDataPath() {
        return this.getClass().getClassLoader().getResource("gold").getPath();
    }

    @Override
    protected boolean skipSpaces() {
        return true;
    }


    @Override
    protected void checkResult(
        @NotNull @NonNls @TestDataFile final String targetDataName,
        @NotNull final PsiFile file
    ) {
        final String fullTargetDataName = "" + File.separator + targetDataName;
        doCheckResult(
            myFullDataPath,
            file,
            checkAllPsiRoots(),
            fullTargetDataName,
            skipSpaces(),
            includeRanges()
        );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final VirtualFile m = new MockVirtualFile(true, myFullDataPath);
        myProject.setBaseDir(m);
    }

}
