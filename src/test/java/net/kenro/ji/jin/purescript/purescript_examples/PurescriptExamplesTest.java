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

    private Processor<File> processor(final boolean passing) {
        return new Processor<File>() {
            @Override
            public boolean process(File file) {
                if (file.isFile()) {
                    try {
                        if (file.getName().endsWith(".purs")) {
                            testExample(file, passing);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail("Failed to read file " + file.getAbsolutePath());
                    }
                }
                return true;
            }
        };
    }

    public void testExamples() {
        String testDataPath = "src/test/resources/purescript_examples";
        FileUtil.processFilesRecursively(new File(testDataPath + "/passing"), processor(true));
//        FileUtil.processFilesRecursively(new File(testDataPath + "/manual/passing"), processor(true));
//        FileUtil.processFilesRecursively(new File(testDataPath + "/failing"), processor(false));
//        FileUtil.processFilesRecursively(new File(testDataPath + "/manual/failing"), processor(false));

//        String additionalTests = "src/test/resources/additional";
//        FileUtil.processFilesRecursively(new File(additionalTests + "/passing"), processor(true));
//        FileUtil.processFilesRecursively(new File(additionalTests + "/perf"), processor(true));
    }

//    public void testPerformance() throws Exception {
//        String perfDir = "src/test/resources/additional/perf/";
//        double emptyFile = perfExample(new File(perfDir + "Empty.purs"));
//        double longFile = perfExample(new File(perfDir + "LongFile.purs"));
//        assertTrue(longFile < emptyFile * 10);
//    }

    public static String readFile(File file) throws IOException {
        String content = new String(FileUtil.loadFileText(file.getCanonicalFile()));
        assertNotNull(content);
        return content;
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

    private void testExample(@NotNull File fileName, final boolean passing) throws Exception {
        PSFile file = (PSFile) createFile(fileName.getName(), readFile(fileName));

        String psiTree = DebugUtil.psiToString(file, false);
        File expectedFile = new File(fileName.getAbsolutePath() + ".psi");
        if (expectedFile.isFile()) {
            String expectedTree = FileUtil.loadFile(expectedFile);
            //if (passing) {
            assertEquals(fileName.getName() + " failed.", expectedTree, psiTree);
            //}
        } else {
//            assert false;  // Only manually.
            FileUtil.writeToFile(new File(fileName.getAbsolutePath() + ".psi"), psiTree);
        }

        if (passing) {
            assertEquals(fileName.getName() + " failed.", true, !psiTree.contains("PsiErrorElement"));
        } else {
            // TODO: type checker.
            // assertEquals(fileName.getName() + " failed.", true, psiTree.contains("PsiErrorElement"));
        }
    }
}

