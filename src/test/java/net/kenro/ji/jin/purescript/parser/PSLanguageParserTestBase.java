package net.kenro.ji.jin.purescript.parser;

import com.intellij.lang.ParserDefinition;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataFile;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;


public abstract class PSLanguageParserTestBase extends ParsingTestCase {

    public PSLanguageParserTestBase(String dataPath, String fileExt, ParserDefinition... definitions) {
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
    protected void doTest(boolean checkResult, boolean shouldPass) {
        doTest(true);
        if (shouldPass) {
            assertFalse(
                    "PsiFile contains error elements",
                    toParseTreeText(myFile, skipSpaces(), includeRanges()).contains("PsiErrorElement")
            );
        }
    }


    @Override
    protected void checkResult(@NonNls @TestDataFile String targetDataName,
                               final PsiFile file) throws IOException {
        doCheckResult(myFullDataPath, file, checkAllPsiRoots(),
                "" + File.separator + targetDataName, skipSpaces(),
                includeRanges());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        VirtualFile m = new MockVirtualFile(true,myFullDataPath);
        myProject.setBaseDir(m);
    }

}
