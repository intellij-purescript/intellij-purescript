package net.kenro.ji.jin.purescript.purescript_examples;


import java.io.File;
import java.io.IOException;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.PsiTestCase;
import com.intellij.util.Processor;

import net.kenro.ji.jin.purescript.file.PSFile;
import org.jetbrains.annotations.NotNull;

public class PurescriptExamplesTest extends PsiTestCase {

    private Processor<File> processor() {
        return file -> {
            if (file.isFile() && file.getName().endsWith(".purs")) {
                try {
                    testExample(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Failed to read file " + file.getAbsolutePath());
                }
            }
            return true;
        };
    }

    public void testExamples() {
        String testDataPath = "src/test/resources/purescript_examples";
        FileUtil.processFilesRecursively(new File(testDataPath + "/passing"), processor());
        FileUtil.processFilesRecursively(new File(testDataPath + "/failing"), processor());
        FileUtil.processFilesRecursively(new File(testDataPath + "/warning"), processor());
        FileUtil.processFilesRecursively(new File(testDataPath + "/docs"), processor());

        String additionalTests = "src/test/resources/additional";
        FileUtil.processFilesRecursively(new File(additionalTests + "/passing"), processor());
//        FileUtil.processFilesRecursively(new File(additionalTests + "/perf"), processor(true));
    }

//    public void testPerformance() throws Exception {
//        String perfDir = "src/test/resources/additional/perf/";
//        double emptyFile = perfExample(new File(perfDir + "Empty.purs"));
//        double longFile = perfExample(new File(perfDir + "LongFile.purs"));
//        assertTrue(longFile < emptyFile * 10);
//    }

    public static String readFile(File file) throws IOException {
        return FileUtil.loadFile(file.getCanonicalFile(), "utf-8", true);
    }

    private double perfExample(@NotNull File fileName) throws Exception {
        int N = 100;
        String text = readFile(fileName);
        createFile(fileName.getName(), text);
        long start = System.nanoTime();
        for (int i = 0; i < N; i++) {
            createFile(fileName.getName(), text);
        }
        long end = System.nanoTime();
        double v = (end - start) * 1e-6 / N;
        System.out.printf("Parsing %s :%.3fms\n", fileName, v);
        return v;
    }

    private void testExample(@NotNull File fileName) throws Exception {
        PSFile file = (PSFile) createFile(fileName.getName(), readFile(fileName));

        String psiTree = DebugUtil.psiToString(file, false);
        File expectedFile = new File(fileName.getAbsolutePath() + ".psi");
        if (expectedFile.isFile()) {
            String expectedTree = readFile(expectedFile);
            assertEquals(fileName.getName() + " failed.", expectedTree, psiTree);
        } else {
//            assert false;  // Only manually.
            FileUtil.writeToFile(new File(fileName.getAbsolutePath() + ".psi"), psiTree);
        }

        assertEquals(fileName.getName() + " failed.", true, !psiTree.contains("PsiErrorElement"));
    }
}

