package org.purescript.psi.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExportedItems

class PSExportListTest : BasePlatformTestCase() {

    fun `test export list contains exported items`() {
        val exportedItems = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo, kind Boolean, class Eq) where"""
        ).getExportedItems()

        assertEquals(3, exportedItems.size)
        assertTrue(exportedItems[0] is PSExportedValue)
        assertTrue(exportedItems[1] is PSExportedKind)
        assertTrue(exportedItems[2] is PSExportedClass)
    }
}
