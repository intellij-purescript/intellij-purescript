package org.purescript.parser

import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.testFramework.ParsingTestCase
import com.intellij.testFramework.TestDataFile
import org.jetbrains.annotations.NonNls
import java.io.File

abstract class PSLanguageParserTestBase :
    ParsingTestCase("parser", "purs", PSParserDefinition()) {
    override fun getTestDataPath(): String =
        "test-data"

    override fun skipSpaces(): Boolean = true

    override fun checkResult(
        @NonNls @TestDataFile targetDataName: String,
        file: PsiFile
    ) {
        val fullTargetDataName = "" + File.separator + targetDataName
        doCheckResult(
            myFullDataPath,
            file,
            checkAllPsiRoots(),
            fullTargetDataName,
            skipSpaces(),
            includeRanges()
        )
    }

    override fun setUp() {
        super.setUp()
        val m: VirtualFile = MockVirtualFile(true, myFullDataPath)
        myProject.baseDir = m
    }
}