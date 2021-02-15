package org.purescript.psi

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile

class PSPositionedDeclarationRefImplTest : BasePlatformTestCase() {
    fun `test knows if its a module export`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module Prelude 
                ( module Control.Applicative
                , pure
                , module1
                , module	Control.Applicative
                ) where
            
            import Control.Applicative (class Applicative, pure, liftA1, unless, when)
            
            module1 = 1
            """.trimIndent()
        ) as PSFile
        val exportDeclarations: Array<PSPositionedDeclarationRefImpl> = file.module.exportDeclarations

        assertNotEmpty(exportDeclarations.toList())
        val moduleExportDeclaration = exportDeclarations.first()
        assertTrue(moduleExportDeclaration.isModuleExport)
        val valuePureExportDeclaration = exportDeclarations[1]
        assertFalse(valuePureExportDeclaration.isModuleExport)
        val valueModule1ExportDeclaration = exportDeclarations[2]
        assertFalse(valueModule1ExportDeclaration.isModuleExport)
        val moduleExportDeclarationWithTab = exportDeclarations[3]
        assertTrue(moduleExportDeclarationWithTab.isModuleExport)
    }
}