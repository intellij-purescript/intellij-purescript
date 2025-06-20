package org.purescript.module.declaration.value.expression

import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.intellij.lang.annotations.Language
import org.purescript.*
import org.purescript.module.declaration.value.expression.namespace.Let

class ExpressionIdentifierReferenceTest : BasePlatformTestCase() {
    // region value declarations

    fun `test resolves value declaration`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x = y
                y = 1
            """.trimIndent()
        )
        val expressionIdentifier = file.getExpressionIdentifier()
        val valueDeclaration = file.getValueDeclarationGroupByName("y")

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test resolves do binder`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                |module Main where
                |one = do
                |   x <- [1]
                |   pure x
            """.trimMargin()
        )
        val expressionIdentifier = file.getExpressionIdentifiers()[1]
        val varBinder = file.getVarBinder()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }
    fun `test resolves to closest do binder`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                |module Main where
                |one = do
                |   x <- [1]
                |   x <- [1]
                |   pure x
            """.trimMargin()
        )
        val expressionIdentifier = file.getExpressionIdentifiers()[1]
        val varBinder = file.getVarBinders()[1]

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }
    fun `test resolves to closest do binder with binders after`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                |module Main where
                |one = do
                |   x <- [1]
                |   x <- [2]
                |   x <- [x]
                |   x <- [3]
                |   pure [4]
            """.trimMargin()
        )
        val expressionIdentifier = file.getExpressionIdentifiers()[0]
        val expected = file.getVarBinders()[1]

        val actual = expressionIdentifier.reference.resolve()
        assertEquals(expected.parent.parent.text, actual?.parent?.parent?.text)
    }

    fun `test resolves imported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs", """
                module Lib (y) where
                y = 1
            """.trimIndent()
        ).getValueDeclarationGroup()
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        val resolve = expressionIdentifier.reference.resolve()
        assertEquals(valueDeclaration, resolve)
    }

    fun `test resolves deep imported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Y.purs", """
                module Y (y) where
                y = 1
            """.trimIndent()
        ).getValueDeclarationGroup()
        myFixture.configureByText(
            "Lib.purs", """
                module Lib (module Y) where
                import Y
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test resolves selective deep imported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Y.purs", """
                module Y (y) where
                y = 1
            """.trimIndent()
        ).getValueDeclarationGroup()
        myFixture.configureByText(
            "Lib.purs", """
                module Lib (module Exports) where
                import Y (y) as Exports
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test does not resolve unexported value declarations`() {
        myFixture.configureByText(
            "Lib.purs", """
                module Lib (z) where
                y = 1
                z = 2
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertNull(expressionIdentifier.reference.resolve())
    }

    fun `test resolves value declaration exported using export all`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs", """
                module Lib where
                y = 1
            """.trimIndent()
        ).getValueDeclarationGroup()
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test does not resolve hidden value declaration`() {
        myFixture.configureByText(
            "Lib.purs", """
                module Lib (y) where
                y = 1
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib hiding (y)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertNull(expressionIdentifier.reference.resolve())
    }

    fun `test resolves imported value declarations when hiding others`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs", """
                module Lib (y, z) where
                y = 1
                z = 2
            """.trimIndent()
        ).getValueDeclarationGroupByName("y")
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib hiding (z)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test does not resolve unimported value declarations`() {
        myFixture.configureByText(
            "Lib.purs", """
                module Lib (y, z) where
                y = 1
                z = 2
            """.trimIndent()
        )
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib (z)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertNull(expressionIdentifier.reference.resolve())
    }

    fun `test resolves explicitly imported and exported value declarations`() {
        val valueDeclaration = myFixture.configureByText(
            "Lib.purs", """
                module Lib (y, z) where
                y = 1
                z = 2
            """.trimIndent()
        ).getValueDeclarationGroupByName("y")
        val expressionIdentifier = myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Lib (y)
                x = y
            """.trimIndent()
        ).getExpressionIdentifier()

        assertEquals(valueDeclaration, expressionIdentifier.reference.resolve())
    }

    fun `test completes value declarations`() {
        myFixture.configureByText(
            "Main.purs", """
                module Main where
                import Foo
                import Bar hiding (y4)
                import Baz (y6)
                y0 = y<caret>
                y1 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs", """
                module Foo (y2) where
                y2 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Bar.purs", """
                module Bar (y4, y5) where
                y4 = 1
                y5 = 1
            """.trimIndent()
        )
        myFixture.configureByText(
            "Baz.purs", """
                module Baz (y6, y7) where
                y6 = 1
                y7 = 1
            """.trimIndent()
        )
        myFixture.testCompletionVariants("Main.purs", "y0", "y1", "y2", "y4", "y5", "y6", "y7")
    }

    // endregion

    // region var binders

    fun `test resolves var binders`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x y = y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test resolves complex var binders`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x (Just y) = y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test resolves record pun binders`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x {y} = y
            """.trimIndent()
        )
        val varBinder = file.getPunBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test resolves record var binder inside record label expression binder`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x {z: y} = y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test resolves record var binder inside record label expression binder in lambda`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x = \{z: y} -> y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test resolves record var binder in let-in inside record label expression binder in lambda`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x = 
                  let y = 1
                  in \{z: y} -> y
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test not resolve to record lable, that is not a pun`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x = 
                  let z = 1
                  in \{z: y} -> z
            """.trimIndent()
        )
        val z = (file.getValueDeclaration().value as Let).valueDeclarationGroups.first()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(z, expressionIdentifier.reference.resolve())
    }

    fun `test resolves var binders used in record expressions`() {
        val file = myFixture.configureByText(
            "Main.purs", """
                module Main where
                x y = {y}
            """.trimIndent()
        )
        val varBinder = file.getVarBinder()
        val expressionIdentifier = file.getExpressionIdentifier()

        assertEquals(varBinder, expressionIdentifier.reference.resolve())
    }

    fun `test completes var binders`() {
        myFixture.configureByText(
            "Main.purs", """
                module Main where
                x y1 y2 = y<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Main.purs", "y1", "y2")
    }

    // endregion

    // region foreign values

    fun `test resolves foreign value declarations`() {
        val file = myFixture.configureByText(
            "Foo.purs", """
                module Foo where
                foreign import x :: Int
                y = x
            """.trimIndent()
        )
        val expressionIdentifier = file.getExpressionIdentifier()
        val foreignValueDeclaration = file.getForeignValueDeclaration()

        assertEquals(
            foreignValueDeclaration, expressionIdentifier.reference.resolve()
        )
    }

    fun `test completes foreign value declarations`() {
        myFixture.configureByText(
            "Bar.purs", """
                module Bar where
                foreign import qux :: Int
                foreign import qut :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs", """
                module Foo where
                import Bar
                y = q<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "qux", "qut")
    }

    // endregion

    // region qualified

    fun `test completes qualified values first`() {
        myFixture.configureByText(
            "Bar.purs", """
                module Bar where
                foreign import y1 :: Int
                foreign import y2 :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Quz.purs", """
                module Quz where
                foreign import y3 :: Int
                y4 = 4
            """.trimIndent()
        )
        myFixture.configureByText(
            "Baz.purs", """
                module Baz (y5, module B) where
                import Bar hiding (y1) as B
                y5 :: Int
                y5 = 5
                foreign import y6 :: Int
            """.trimIndent()
        )
        myFixture.configureByText(
            "Foo.purs", """
                module Foo where
                import Bar
                import Quz as Q
                import Baz as B
                y0 = B.y<caret>
            """.trimIndent()
        )

        myFixture.testCompletionVariants("Foo.purs", "y1", "y2", "y3", "y4", "y5")
    }

    // endregion

    fun `test rename argument`() {
        doTestRename(
            """
                |module Main where
                |
                |foo {-caret-}a = a
            """.trimMargin(),
            "b",
            """
                |module Main where
                |
                |foo b = b
            """.trimMargin()
        )
    }

    fun `test rename argument that is subject of case expression`() {
        doTestRename(
            """
                |module Main where
                |
                |foo {-caret-}a b = case a of
                |  r -> b
            """.trimMargin(),
            "c",
            """
                |module Main where
                |
                |foo c b = case c of
                |  r -> b
            """.trimMargin()
        )
    }

    fun `test rename argument that is subject of case branch expression`() {
        doTestRename(
            """
                |module Main where
                |
                |foo a {-caret-}b = case a of
                |  r -> b
            """.trimMargin(),
            "c",
            """
                |module Main where
                |
                |foo a c = case a of
                |  r -> c
            """.trimMargin()
        )
    }

    fun `test rename subject of case branch expression that is argument `() {
        doTestRename(
            """
                |module Main where
                |
                |foo a b = case a of
                |  r -> {-caret-}b
            """.trimMargin(),
            "c",
            """
                |module Main where
                |
                |foo a c = case a of
                |  r -> c
            """.trimMargin()
        )
    }

    private fun doTestRename(
        @Language("Purescript") before: String,
        newName: String,
        @Language("Purescript") after: String
    ) {
        myFixture.configureByText("Main.purs", before.replace("{-caret-}", "<caret>"))
        myFixture.renameElementAtCaret(newName)
        myFixture.checkResult("Main.purs", after, true)
        PsiTestUtil.checkPsiStructureWithCommit(myFixture.file, PsiTestUtil::checkPsiMatchesTextIgnoringNonCode)
    }

}
