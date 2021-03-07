package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile

class PSImportDeclarationImplTest : BasePlatformTestCase() {

    fun `test resolve to module in root directory`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Foo.purs",
            """
            module Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!!

        TestCase.assertEquals("Foo", psModule.name)
    }

    fun `test dont crash if module not found`() {
        val mainFile = myFixture.addFileToProject(
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


    fun `test resolve to module in subdirectory`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Bar.Foo
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Bar/Foo.purs",
            """
            module Bar.Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Bar.Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!!

        TestCase.assertEquals("Bar.Foo", psModule.name)
    }

    fun `test resolve to module with correct module name when there is competing files`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Bar.Foo
            """.trimIndent()
        ) as PSFile
        myFixture.addFileToProject(
            "Bar/Foo.purs",
            """
            module Bar.Foo where
            """.trimIndent()
        )
        myFixture.addFileToProject(
            "Foo.purs",
            """
            module Foo where
            """.trimIndent()
        )
        val psImportDeclaration = mainFile.module.getImportDeclarationByName("Bar.Foo")!!

        val resolve = psImportDeclaration.reference.resolve()
        val psModule = resolve!!

        TestCase.assertEquals("Bar.Foo", psModule.name)
    }

    fun `test knows about imported names`() {
        val mainFile = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo hiding (x)
            import Bar
            import Buz (x)
            import Fuz (hiding)
            """.trimIndent()
        ) as PSFile

        val foo = mainFile.module.getImportDeclarationByName("Foo")!!
        assertTrue(foo.isHiding)
        assertContainsElements(foo.namedImports, "x")

        val bar = mainFile.module.getImportDeclarationByName("Bar")!!
        assertFalse(bar.isHiding)
        assertDoesntContain(bar.namedImports, "x")

        val buz = mainFile.module.getImportDeclarationByName("Buz")!!
        assertFalse(buz.isHiding)
        assertContainsElements(buz.namedImports, "x")

        val fuz = mainFile.module.getImportDeclarationByName("Fuz")!!
        assertFalse(fuz.isHiding)
        assertContainsElements(fuz.namedImports, "hiding")
    }

    fun `test parses import declaration children`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Adam
                import Adam.Nested
                import Adam.Aliased as A
                import Bertil ()
                import Bertil.Nested ()
                import Bertil.Aliased () as B
                import Caesar hiding ()
                import Caesar.Nested hiding ()
                import Caesar.Aliased hiding () as C
            """.trimIndent()
        ) as PSFile
        val importDeclarations = file.module.importDeclarations

        // import Adam
        importDeclarations[0].run {
            assertEquals("Adam", importName!!.name)
            assertNull(importList)
            assertFalse(isHiding)
            assertNull(importAlias)
        }

        // import Adam.Nested
        importDeclarations[1].run {
            assertEquals("Adam.Nested", importName!!.name)
            assertNull(importList)
            assertFalse(isHiding)
            assertNull(importAlias)
        }

        // import Adam.Aliased as A
        importDeclarations[2].run {
            assertEquals("Adam.Aliased", importName!!.name)
            assertNull(importList)
            assertFalse(isHiding)
            assertEquals("A", importAlias!!.name)
        }

        // import Bertil (b)
        importDeclarations[3].run {
            assertEquals("Bertil", importName!!.name)
            assertNotNull(importList)
            assertFalse(isHiding)
            assertNull(importAlias)
        }

        // import Bertil.Nested (b)
        importDeclarations[4].run {
            assertEquals("Bertil.Nested", importName!!.name)
            assertNotNull(importList)
            assertFalse(isHiding)
            assertNull(importAlias)
        }

        // import Bertil.Aliased (b) as B
        importDeclarations[5].run {
            assertEquals("Bertil.Aliased", importName!!.name)
            assertNotNull(importList)
            assertFalse(isHiding)
            assertEquals("B", importAlias!!.name)
        }

        // import Caesar hiding (c)
        importDeclarations[6].run {
            assertEquals("Caesar", importName!!.name)
            assertNotNull(importList)
            assertTrue(isHiding)
            assertNull(importAlias)
        }

        // import Caesar.Nested hiding (c)
        importDeclarations[7].run {
            assertEquals("Caesar.Nested", importName!!.name)
            assertNotNull(importList)
            assertTrue(isHiding)
            assertNull(importAlias)
        }

        // import Caesar.Aliased hiding (c) as C
        importDeclarations[8].run {
            assertEquals("Caesar.Aliased", importName!!.name)
            assertNotNull(importList)
            assertTrue(isHiding)
            assertEquals("C", importAlias!!.name)
        }
    }
}
