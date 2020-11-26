package net.kenro.ji.jin.purescript.parser;

import com.intellij.lang.ParserDefinition;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public abstract class PSLanguageParserTestBase extends ParsingTestCase {

    public PSLanguageParserTestBase(
        final String dataPath,
        final String fileExt,
        final ParserDefinition... definitions
    ) {
        super(dataPath, fileExt, definitions);
    }

    @Override
    protected String getTestDataPath() {
        return this.getClass().getClassLoader().getResource("gold").getPath();
    }

    @Override
    protected boolean skipSpaces() {
        return true;
    }

    /**
     * Perform a test. Add tests that should work but does not work yet with
     * doTest(false, false).
     */
    protected void doTestOld(final boolean checkResult, final boolean shouldPass) {
        doTest(true);
        if (shouldPass) {
            assertFalse(
                "PsiFile contains error elements",
                toParseTreeText(myFile, skipSpaces(), includeRanges()).contains(
                    "PsiErrorElement")
            );
        }
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
