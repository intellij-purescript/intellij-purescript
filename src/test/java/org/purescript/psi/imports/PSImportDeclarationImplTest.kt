package org.purescript.psi.imports

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.getImportDeclaration
import org.purescript.getImportDeclarations
import org.purescript.getModule

class PSImportDeclarationImplTest : BasePlatformTestCase() {

    fun `test resolve to module in root directory`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ).getModule()
        myFixture.addFileToProject(
            "Foo.purs",
            """
            module Foo where
            """.trimIndent()
        )
        val psImportDeclaration = module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!!

        TestCase.assertEquals("Foo", psModule.name)
    }

    fun `test dont crash if module not found`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo
            """.trimIndent()
        ).getModule()

        val psImportDeclaration = module.getImportDeclarationByName("Foo")!!

        val psModule = psImportDeclaration.reference.resolve()
        TestCase.assertNull(psModule)
    }


    fun `test resolve to module in subdirectory`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Bar.Foo
            """.trimIndent()
        ).getModule()
        myFixture.addFileToProject(
            "Bar/Foo.purs",
            """
            module Bar.Foo where
            """.trimIndent()
        )
        val psImportDeclaration = module.getImportDeclarationByName("Bar.Foo")!!

        val psModule = psImportDeclaration.reference.resolve()!!

        TestCase.assertEquals("Bar.Foo", psModule.name)
    }

    fun `test resolve to module with correct module name when there is competing files`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Bar.Foo
            """.trimIndent()
        ).getModule()
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
        val psImportDeclaration = module.getImportDeclarationByName("Bar.Foo")!!

        val resolve = psImportDeclaration.reference.resolve()
        val psModule = resolve!!

        TestCase.assertEquals("Bar.Foo", psModule.name)
    }

    fun `test knows about imported names`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """
            module Main where
            import Foo hiding (x)
            import Bar
            import Buz (x)
            import Fuz (hiding)
            """.trimIndent()
        ).getModule()

        val foo = module.getImportDeclarationByName("Foo")!!
        assertTrue(foo.isHiding)
        assertContainsElements(foo.namedImports, "x")

        val bar = module.getImportDeclarationByName("Bar")!!
        assertFalse(bar.isHiding)
        assertDoesntContain(bar.namedImports, "x")

        val buz = module.getImportDeclarationByName("Buz")!!
        assertFalse(buz.isHiding)
        assertContainsElements(buz.namedImports, "x")

        val fuz = module.getImportDeclarationByName("Fuz")!!
        assertFalse(fuz.isHiding)
        assertContainsElements(fuz.namedImports, "hiding")
    }

    fun `test parses import declaration children`() {
        val importDeclarations = myFixture.configureByText(
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
        ).getImportDeclarations()

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

    fun `test uses alias name if exists`() {
        val importDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Foo.Bar as FB
            """.trimIndent()
        ).getImportDeclaration()

        assertEquals("FB", importDeclaration.name)
    }

    fun `test module name if alias doesn't exist`() {
        val importDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                import Foo.Bar
            """.trimIndent()
        ).getImportDeclaration()

        assertEquals("Foo.Bar", importDeclaration.name)
    }
}
