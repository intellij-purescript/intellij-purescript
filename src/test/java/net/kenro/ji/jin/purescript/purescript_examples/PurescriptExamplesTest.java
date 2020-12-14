package net.kenro.ji.jin.purescript.purescript_examples;


import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.util.Processor;
import net.kenro.ji.jin.purescript.PSLanguage;
import net.kenro.ji.jin.purescript.file.PSFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PurescriptExamplesTest extends BasePlatformTestCase {

    private Processor<File> processor() {
        return file -> {
            if (file.isFile() && file.getName().endsWith(".purs")) {
                try {
                    testExample(file);
                } catch (final Exception e) {
                    e.printStackTrace();
                    fail("Failed to read file " + file.getAbsolutePath());
                }
            }
            return true;
        };
    }

    public void testExamples() {
        final String testDataPath = "src/test/resources/purescript_examples";
        FileUtil.processFilesRecursively(new File(testDataPath + "/passing"), processor());
        FileUtil.processFilesRecursively(new File(testDataPath + "/failing"), processor());
        FileUtil.processFilesRecursively(new File(testDataPath + "/warning"), processor());
        FileUtil.processFilesRecursively(new File(testDataPath + "/docs"), processor());

        final String additionalTests = "src/test/resources/additional";
        FileUtil.processFilesRecursively(new File(additionalTests + "/passing"), processor());
    }


    public static String readFile(final File file) throws IOException {
        return FileUtil.loadFile(file.getCanonicalFile(), "utf-8", true);
    }


    private void testExample(@NotNull final File fileName) throws Exception {
        final PSFile file = (PSFile) createLightFile(fileName.getName(),
            PSLanguage.INSTANCE,
            readFile(fileName));

        final String psiTree = DebugUtil.psiToString(file, false);
        final File expectedFile = new File(fileName.getAbsolutePath() + ".psi");
        if (expectedFile.isFile()) {
            final String expectedTree = readFile(expectedFile);
            assertEquals(fileName.getName() + " failed.", expectedTree, psiTree);
        } else {
            FileUtil.writeToFile(new File(fileName.getAbsolutePath() + ".psi"), psiTree);
        }

        assertEquals(fileName.getName() + " failed.", true, !psiTree.contains("PsiErrorElement"));
    }
}

