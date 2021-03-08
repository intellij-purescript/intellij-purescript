package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSExportedItemTest : BasePlatformTestCase() {

    fun `test parses exported class`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (class Foo) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedClass)
        TestCase.assertEquals("Foo", exportedItem.name)
    }

    fun `test parses exported data`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedData)
        val exportedData = exportedItem as PSExportedData

        TestCase.assertEquals("Foo", exportedItem.name)
        TestCase.assertNull(exportedData.dataMemberList)
    }

    fun `test parses exported data with all members`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo(..)) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedData)
        val exportedData = exportedItem as PSExportedData

        TestCase.assertEquals("Foo", exportedItem.name)
        val dataMemberList = exportedData.dataMemberList

        TestCase.assertNotNull(exportedData.dataMemberList)
        TestCase.assertNotNull(dataMemberList!!.doubleDot)
        TestCase.assertTrue(dataMemberList.dataMembers.isEmpty())
    }

    fun `test parses exported data with some members`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo(Bar, Baz)) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedData)
        val exportedData = exportedItem as PSExportedData

        TestCase.assertEquals("Foo", exportedItem.name)
        val dataMemberList = exportedData.dataMemberList

        TestCase.assertNotNull(exportedData.dataMemberList)
        TestCase.assertNull(dataMemberList!!.doubleDot)
        TestCase.assertEquals(2, dataMemberList.dataMembers.size)
    }

    fun `test parses exported kind`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (kind Foo) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedKind)
        TestCase.assertEquals("Foo", exportedItem.name)
    }

    fun `test parses exported module`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (module Foo) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedModule)
        TestCase.assertEquals("Foo", exportedItem.name)
    }

    fun `test parses exported operator`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main ((<~>)) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedOperator)
        TestCase.assertEquals("<~>", exportedItem.name)
    }

    fun `test parses exported type`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (type (<<=>>)) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedType)
        TestCase.assertEquals("<<=>>", exportedItem.name)
    }

    fun `test parses exported value`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where"""
        ) as PSFile
        val exportedItem = file.module.exportList!!.exportedItems.single()

        TestCase.assertTrue(exportedItem is PSExportedValue)
        TestCase.assertEquals("foo", exportedItem.name)
    }
}
