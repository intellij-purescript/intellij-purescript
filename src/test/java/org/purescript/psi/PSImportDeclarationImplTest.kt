package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSImportDeclarationImplTest : BasePlatformTestCase() {

    fun `test var can resolve to module next to this one on file system`() {
        val mainFile = myFixture.configureByText(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ) as PSFile
        myFixture.configureByText(
            "Foo.purs",
            """
            module Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!! as PSModule

        TestCase.assertEquals("Foo", psModule.name)
    }

    fun `test dont crash if module not found`() {
        val mainFile = myFixture.configureByText(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ) as PSFile

        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()
        TestCase.assertNull(psModule)
    }
}