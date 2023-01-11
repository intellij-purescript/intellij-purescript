package org.purescript.psi.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExportedItems

class PSExportListTest : BasePlatformTestCase() {

    fun `test export list contains exported items`() {
        val exportedItems = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo, class Eq) where"""
        ).getExportedItems()

        assertEquals(2, exportedItems.size)
        assertTrue(exportedItems[0] is ExportedValue.Psi)
        assertTrue(exportedItems[1] is ExportedClass.Psi)
    }
}
