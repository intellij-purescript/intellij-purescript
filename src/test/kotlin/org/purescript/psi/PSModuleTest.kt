package org.purescript.psi

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import org.purescript.*

class PSModuleTest : BasePlatformTestCase() {

    private fun PsiFile.exportedForeignValueDeclarationNames(): List<String> =
        getModule().exportedForeignValueDeclarations.map { it.name }


    fun `test one word name`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """module Main where"""
        ).getModule()
        assertEquals("Main", module.name)
    }

    fun `test two word name`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main where"""
        ).getModule()
        assertEquals("My.Main", module.name)
    }

    fun `test be able to find no exported names`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main where"""
        ).getModule()
        assertEquals(0, module.exportedNames.size)
    }

    fun `test be able to find one exported names`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """
            module My.Main (x) where
            x  = 1
            """.trimIndent()
        ).getModule()
        assertEquals(1, module.exportedNames.size)
    }

    fun `test be able to find two exported names`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, y) where
               x = 1
               y = 2
            """.trimIndent()
        ).getModule()
        assertEquals(2, module.exportedNames.size)
        assertContainsElements(module.exportedNames, "x", "y")
    }

    fun `test do not count module export as exported names`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, module Y) where
               
               import Y
               
               x = 1
            """.trimIndent()
        ).getModule()
        assertEquals(1, module.exportedNames.size)
    }

    fun `test knows what modules get reexported`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """module My.Main (x, module Y) where
               
               import Y
               
               x = 1
            """.trimIndent()
        ).getModule()
        assertEquals(1, module.reexportedModuleNames.size)
        assertContainsElements(module.reexportedModuleNames, "Y")
    }

    fun `test finds doc comment`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """-- | This is
               -- | main
               module My.Main (x, y) where
            """.trimIndent()
        ).getModule()

        assertEquals("-- | This is", module.docComments[0].text)
        assertEquals("-- | main", module.docComments[1].text)
    }

    fun `test finds data declarations`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """ module Main where
                data Foo = Bar
                data Qux a b = Baz (a -> b)
            """.trimIndent()
        ).getModule()
        assertSize(2, module.cache.dataDeclarations)
    }

    fun `test finds foreign value declarations`() {
        val module = myFixture.addFileToProject(
            "Main.purs",
            """ module Main (split) where
                
                -- | Returns the substrings of the second string separated
                foreign import split :: Pattern -> String -> Array String
            """.trimIndent()
        ).getModule()
        TestCase.assertEquals(1, module.cache.foreignValueDeclarations.size)
    }

    fun `test exported value declarations (exports all)`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                a = 1
                b = 2
            """.trimIndent()
        ).getModule()
        val actualExportedValueDeclarationNames = module.exportedValueDeclarations.map { it.name }
        assertContainsElements(actualExportedValueDeclarationNames, "a", "b")
    }

    fun `test exported value declarations (exports some)`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (b) where
                a = 1
                b = 2
            """.trimIndent()
        ).getModule()
        val actualExportedValueDeclarationNames = module.exportedValueDeclarations.map { it.name }
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
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar
            """.trimIndent()
        ).getModule()
        val actualExportedValueDeclarationNames = module.exportedValueDeclarations.map { it.name }
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
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar (a)
            """.trimIndent()
        ).getModule()
        val actualExportedValueDeclarationNames = module.exportedValueDeclarations.map { it.name }
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
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar hiding (a)
            """.trimIndent()
        ).getModule()
        val actualExportedValueDeclarationNames = module.exportedValueDeclarations.map { it.name }
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
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar) where
                import Bar
            """.trimIndent()
        ).getModule()
        val actualExportedValueDeclarationNames = module.exportedValueDeclarations.map { it.name }
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
        ).getModule()
        val quxExportedValueDeclarationNames = qux.exportedValueDeclarations.map { it.name }
        assertSameElements(quxExportedValueDeclarationNames, "a", "b")

        val bar = myFixture.configureByText(
            "Bar.purs",
            """
                module Bar (module Qux, d) where
                import Qux hiding (b)
                d = 4
                e = 5
            """.trimIndent()
        ).getModule()
        val barExportedValueDeclarationNames = bar.exportedValueDeclarations.map { it.name }
        assertSameElements(barExportedValueDeclarationNames, "a", "d")

        val foo = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Bar, g) where
                import Bar (a)
                f = 6
                g = 7
            """.trimIndent()
        ).getModule()
        val fooExportedValueDeclarationNames = foo.exportedValueDeclarations.map { it.name }
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

        assertSize(2, module.cache.newTypeDeclarations)
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

    fun `test finds class declarations`() {
        val classDeclarations = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                class Class1
                class Class2
            """.trimIndent()
        ).getClassDeclarations()

        assertSize(2, classDeclarations)
    }

    fun `test finds exported class declarations`() {
        val exportedClassDeclarations = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (class Class1, class Class2) where
                class Class1
                class Class2
                class Class3
            """.trimIndent()
        ).getExportedClassDeclarations()

        assertSize(2, exportedClassDeclarations)
    }

    fun `test finds type synonym declarations`() {
        val typeSynonymDeclarations = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                type T1 = Int
                type T2 = String
            """.trimIndent()
        ).getTypeSynonymDeclarations()

        assertSize(2, typeSynonymDeclarations)
    }

    fun `test finds exported type synonym declarations`() {
        val exportedTypeSynonymDeclarations = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (Silver, Tail) where
                type Silver = Int
                type Stream = Boolean
                type Tail = String
            """.trimIndent()
        ).getModule().exportedTypeSynonymDeclarations

        assertSize(2, exportedTypeSynonymDeclarations)
    }

    fun `test rename simple`() {
        myFixture.configureByText(
            "Foo.purs",
            """
                module <caret>Foo where
            """.trimIndent()
        )
        myFixture.renameElementAtCaret("Bar")
        myFixture.checkResult(
            """
                module Bar where
            """.trimIndent()
        )
    }

    fun `test rename qualified`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module <caret>Foo where
            """.trimIndent()
        ).getModule()
        myFixture.renameElementAtCaret("Foo.Bar")

        assertEquals("Foo.Bar", module.name)
    }

    fun `test finds newtype constructors`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                newtype A = B Int
            """.trimIndent()
        ).getModule()

        assertSize(1, module.cache.newTypeConstructors)
    }

    fun `test finds data constructors`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo where
                data A
                    = B
                    | C
                    | D
            """.trimIndent()
        ).getModule()

        assertSize(3, module.cache.dataConstructors)
    }

    fun `test finds exported newtype constructors`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (A(A), B(..), C) where
                newtype A = A Int
                newtype B = B Int
                newtype C = C Int
            """.trimIndent()
        ).getModule()

        assertSize(2, module.exportedNewTypeConstructors)
    }

    fun `test finds exported data constructors`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (A(B, C)) where
                data A 
                    = A Int
                    | B Int
                    | C Int
            """.trimIndent()
        ).getModule()

        assertSize(2, module.exportedDataConstructors)
    }
    fun `test exports self`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (module Foo) where
            """.trimIndent()
        ).getModule()
        assertTrue(module.exportsSelf)
    }
    fun `test dont exports self`() {
        val module = myFixture.configureByText(
            "Foo.purs",
            """
                module Foo (f) where
                f = 1
            """.trimIndent()
        ).getModule()
        assertFalse(module.exportsSelf)
    }
}

