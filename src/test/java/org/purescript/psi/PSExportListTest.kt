package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSExportListTest : BasePlatformTestCase() {

    fun `test export list contains exported items`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo, kind Boolean, class Eq) where"""
        ) as PSFile
        val exportedItems = file.module.exportList!!.exportedItems

        TestCase.assertEquals(3, exportedItems.size)
        TestCase.assertTrue(exportedItems[0] is PSExportedValue)
        TestCase.assertTrue(exportedItems[1] is PSExportedKind)
        TestCase.assertTrue(exportedItems[2] is PSExportedClass)
    }
}
