package org.purescript.psi.exports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.getExportedItem

class PSExportedItemTest : BasePlatformTestCase() {

    fun `test parses exported class`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (class Foo) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedClass)
        assertEquals("Foo", exportedItem.name)
    }

    fun `test parses exported data`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedData)
        val exportedData = exportedItem as PSExportedData

        assertEquals("Foo", exportedItem.name)
        assertNull(exportedData.dataMemberList)
    }

    fun `test parses exported data with all members`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo(..)) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedData)
        val exportedData = exportedItem as PSExportedData

        assertEquals("Foo", exportedItem.name)
        val dataMemberList = exportedData.dataMemberList

        assertNotNull(exportedData.dataMemberList)
        assertNotNull(dataMemberList!!.doubleDot)
        assertTrue(dataMemberList.dataMembers.isEmpty())
    }

    fun `test parses exported data with some members`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo(Bar, Baz)) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedData)
        val exportedData = exportedItem as PSExportedData

        assertEquals("Foo", exportedItem.name)
        val dataMemberList = exportedData.dataMemberList

        assertNotNull(exportedData.dataMemberList)
        assertNull(dataMemberList!!.doubleDot)
        assertEquals(2, dataMemberList.dataMembers.size)
    }

    fun `test parses exported kind`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (kind Foo) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedKind)
        assertEquals("Foo", exportedItem.name)
    }

    fun `test parses exported module`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (module Foo) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedModule)
        assertEquals("Foo", exportedItem.name)
    }

    fun `test parses exported operator`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main ((<~>)) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedOperator)
        assertEquals("<~>", exportedItem.name)
    }

    fun `test parses exported type`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (type (<<=>>)) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedType)
        assertEquals("<<=>>", exportedItem.name)
    }

    fun `test parses exported value`() {
        val exportedItem = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where"""
        ).getExportedItem()

        assertTrue(exportedItem is PSExportedValue)
        assertEquals("foo", exportedItem.name)
    }
}
