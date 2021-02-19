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
        TestCase.assertTrue(file.module.exportList!!.exportedItems.single() is PSExportedData)
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
