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
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedClass)
    }

    fun `test parses exported data`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo) where"""
        ) as PSFile
        val exportedData = file.module.exportList!!.exportedItems.single() as PSExportedData

        TestCase.assertNull(exportedData.dataMemberList)
    }

    fun `test parses exported data with all members`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (Foo(..)) where"""
        ) as PSFile
        val exportedData = file.module.exportList!!.exportedItems.single() as PSExportedData
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
        val exportedData = file.module.exportList!!.exportedItems.single() as PSExportedData
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
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedKind)
    }

    fun `test parses exported module`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (module Foo) where"""
        ) as PSFile
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedModule)
    }

    fun `test parses exported operator`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main ((<~>)) where"""
        ) as PSFile
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedOperator)
    }

    fun `test parses exported type`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (type (<<=>>)) where"""
        ) as PSFile
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedType)
    }

    fun `test parses exported value`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main (foo) where"""
        ) as PSFile
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedValue)
    }
}
