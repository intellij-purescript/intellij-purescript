package org.purescript.psi

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.file.PSFile
import org.purescript.getExportedDataDeclarations
import org.purescript.getModule

class PSModuleTest : BasePlatformTestCase() {

    private fun PsiFile.exportedForeignValueDeclarationNames(): List<String> =
        getModule().exportedForeignValueDeclarations.map { it.name!! }


    fun `test one word name`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module Main where"""
        ) as PSFile
        assertEquals("Main", file.module.name)
    }

    fun `test two word name`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main where"""
        ) as PSFile
        assertEquals("My.Main", file.module.name)
    }

    fun `test be able to find no exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main where"""
        ) as PSFile
        assertEquals(0, file.module.exportedNames.size)
    }

    fun `test be able to find one exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """
            module My.Main (x) where
            x  = 1
            """.trimIndent()
        ) as PSFile
        assertEquals(1, file.module.exportedNames.size)
    }

    fun `test be able to find two exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, y) where
               x = 1
               y = 2
            """.trimIndent()
        ) as PSFile
        assertEquals(2, file.module.exportedNames.size)
        assertContainsElements(file.module.exportedNames, "x", "y")
    }

    fun `test do not count module export as exported names`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, module Y) where
               
               import Y
               
               x = 1
            """.trimIndent()
        ) as PSFile
        assertEquals(1, file.module.exportedNames.size)
    }

    fun `test knows what modules get reexported`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, module Y) where
               
               import Y
               
               x = 1
            """.trimIndent()
        ) as PSFile
        assertEquals(1, file.module.reexportedModuleNames.size)
        assertContainsElements(file.module.reexportedModuleNames, "Y")
    }

    fun `test finds doc comment`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main
               module My.Main (x, y) where
            """.trimIndent()
        ) as PSFile

        assertEquals("-- | This is", file.module.docComments[0].text)
        assertEquals("-- | main", file.module.docComments[1].text)
    }

    fun `test finds data declarations`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """ module Main where
                data Foo = Bar
                data Qux a b = Baz (a -> b)
            """.trimIndent()
        ) as PSFile
        assertSize(2, file.module.dataDeclarations)
    }

    fun `test finds foreign value declarations`() {
        val file = myFixture.addFileToProject(
            "Main.purs",
            """ module Main (split) where
                
                -- | Returns the substrings of the second string separated
                foreign import split :: Pattern -> String -> Array String
            """.trimIndent()
        ) as PSFile
        TestCase.assertEquals(1, file.module.foreignValueDeclarations.size)
    }

    fun `test exported value declarations (exports all)`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                a = 1
                b = 2
            """.trimIndent()
        ) as PSFile
        val actualExportedValueDeclarationNames = file.module.exportedValueDeclarations.map { it.name }
        assertContainsElements(actualExportedValueDeclarationNames, "a", "b")
    }

    fun `test exported value declarations (exports some)`() {
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (b) where
                a = 1
                b = 2
            """.trimIndent()
        ) as PSFile
        val actualExportedValueDeclarationNames = file.module.exportedValueDeclarations.map { it.name }
        assertSameElements(actualExportedValueDeclarationNames, "b")
    }

    fun `test exported value declarations (re-export entire module)`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar where
                a = 1
                b = 2
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar
            """.trimIndent()
        ) as PSFile
        val actualExportedValueDeclarationNames = file.module.exportedValueDeclarations.map { it.name }
        assertSameElements(actualExportedValueDeclarationNames, "a", "b")
    }

    fun `test exported value declarations (re-export some of module)`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (a, b) where
                a = 1
                b = 2
                c = 3
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar (a)
            """.trimIndent()
        ) as PSFile
        val actualExportedValueDeclarationNames = file.module.exportedValueDeclarations.map { it.name }
        assertSameElements(actualExportedValueDeclarationNames, "a")
    }

    fun `test exported value declarations (hiding some of re-exported module)`() {
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (a, b) where
                a = 1
                b = 2
                c = 3
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar hiding (a)
            """.trimIndent()
        ) as PSFile
        val actualExportedValueDeclarationNames = file.module.exportedValueDeclarations.map { it.name }
        assertSameElements(actualExportedValueDeclarationNames, "b")
    }

    fun `test exported value declarations (nested re-exported module)`() {
        myFixture.configureByText(
            "Qux.purs",
            """
                module Qux where
                a = 1
                b = 2
                c = 3
            """.trimIndent()
        )
        myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (module Qux) where
                import Qux
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar
            """.trimIndent()
        ) as PSFile
        val actualExportedValueDeclarationNames = file.module.exportedValueDeclarations.map { it.name }
        assertSameElements(actualExportedValueDeclarationNames, "a", "b", "c")
    }

    fun `test exported value declarations (combination)`() {
        val qux = myFixture.configureByText(
            "Qux.purs",
            """
                module Qux (a, b) where
                a = 1
                b = 2
                c = 3
            """.trimIndent()
        ) as PSFile
        val quxExportedValueDeclarationNames = qux.module.exportedValueDeclarations.map { it.name }
        assertSameElements(quxExportedValueDeclarationNames, "a", "b")

        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (module Qux, d) where
                import Qux hiding (b)
                d = 4
                e = 5
            """.trimIndent()
        ) as PSFile
        val barExportedValueDeclarationNames = bar.module.exportedValueDeclarations.map { it.name }
        assertSameElements(barExportedValueDeclarationNames, "a", "d")

        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar, g) where
                import Bar (a)
                f = 6
                g = 7
            """.trimIndent()
        ) as PSFile
        val fooExportedValueDeclarationNames = foo.module.exportedValueDeclarations.map { it.name }
        assertSameElements(fooExportedValueDeclarationNames, "a", "g")
    }

    fun `test exports all foreign values`() {
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                foreign import foo :: Int
                foreign import bar :: Int
            """.trimIndent()
        ).exportedForeignValueDeclarationNames()
        assertSameElements(foo, "foo", "bar")
    }

    fun `test exports some foreign values`() {
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (bar, qux) where
                foreign import foo :: Int
                foreign import bar :: Int
                foreign import qux :: Int
            """.trimIndent()
        ).exportedForeignValueDeclarationNames()
        assertSameElements(foo, "bar", "qux")
    }

    fun `test re-exports some foreign values`() {
        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                foreign import foo :: Int
                foreign import bar :: Int
                foreign import qux :: Int
            """.trimIndent()
        ).exportedForeignValueDeclarationNames()

        assertSameElements(foo, "foo", "bar", "qux")

        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (module Foo) where
                import Foo hiding (foo, bar)
            """.trimIndent()
        ).exportedForeignValueDeclarationNames()

        assertSameElements(bar, "qux")
    }

    fun `test finds newtype declarations`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                newtype AppM a = AppM (ReaderT Env Aff a)
                newtype ReplaceLeft r = ReplaceLeft { | r }
            """.trimIndent()
        ).getModule()

        assertSize(2, module.newTypeDeclarations)
    }

    fun `test finds exported newtype declarations`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar) where
                newtype Foo = Foo Int
                newtype Bar = Bar Int
            """.trimIndent()
        ).getModule()
        val newTypeDeclaration = module.exportedNewTypeDeclarations.single()

        assertEquals("Bar", newTypeDeclaration.name)
    }

    fun `test finds exported data declarations`() {
        val dataDeclaration = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Bar(..)) where
                data Foo = Foo Int
                data Bar = Bar Int
            """.trimIndent()
        ).getExportedDataDeclarations().single()

        assertEquals("Bar", dataDeclaration.name)
    }
}

