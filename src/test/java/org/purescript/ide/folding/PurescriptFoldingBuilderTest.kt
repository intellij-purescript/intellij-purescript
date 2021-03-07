package org.purescript.ide.folding

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class PurescriptFoldingBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "test-data/folding"

    fun `test folds two imports`() {
        myFixture.testFolding("$testDataPath/imports_2.purs")
    }

    fun `test folds imports with line breaks`() {
        myFixture.testFolding("$testDataPath/imports_line_breaks.purs")
    }

    fun `test doesn't fold single import`() {
        myFixture.testFolding("$testDataPath/imports_single.purs")
    }
}
