package net.kenro.ji.jin.purescript

import net.kenro.ji.jin.purescript.file.PSFile
import net.kenro.ji.jin.purescript.parser.PSLanguageParserTestBase
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl

class ReferenceTest : PSLanguageParserTestBase() {
    fun testFindTopLevelValueDeclarationWithName() {
        val file = createFile(
            "Main.purs",
            """
                  module Main where
                  x = 1
                  y = 2
                  """.trimIndent()
        ) as PSFile
        val valueDeclarations = file.topLevelValueDeclarations
        assertSize(2, valueDeclarations.keys)
        assertNotNull(valueDeclarations["x"])
        assertNotNull(valueDeclarations["y"])
    }

    fun testFindParametersForValueDeclaration() {
        val file = createFile(
            "Main.purs",
            """
                  module Main where
                  fn x (z) (Just n) = x + y
                  y = 2
                  """.trimIndent()
        ) as PSFile
        val valueDeclarations = file.topLevelValueDeclarations
        val fn = valueDeclarations["fn"]
        val parameterDeclarations = fn!!.declaredIdentifiersInParameterList
        assertContainsElements(parameterDeclarations.keys, "x", "z", "n")
        assertDoesntContain(parameterDeclarations.keys, "fn", "y", "Just")
        val x = parameterDeclarations["x"]
        assertEquals("x", x!!.name)
    }

    fun testIdentifierCanResolveToToplevelValueDeclaration() {
        val file = createFile(
            "Main.purs",
            """
                  module Main where
                  x = 1
                  y = x
                  """.trimIndent()
        ) as PSFile
        val psIdentifier = file.findElementAt(28)!!.parent as PSIdentifierImpl
        val reference = psIdentifier.reference
        assertTrue(
            "identifier reference should include the whole name in its range",
            reference!!.rangeInElement.contains(0)
        )
        val resolved = reference.resolve()
        assertInstanceOf(resolved, PSValueDeclarationImpl::class.java)
        assertEquals("x", (resolved as PSValueDeclarationImpl?)!!.name)
    }

    fun testIdentifierCanResolveToParameter() {
        val file = createFile(
            "Main.purs",
            """
                  module Main where
                  z = 1
                  y x = x
                  """.trimIndent()
        ) as PSFile
        val psIdentifier = file.findElementAt(30)!!.parent as PSIdentifierImpl
        val reference = psIdentifier.reference
        assertTrue(
            "identifier reference should include the whole name in its range",
            reference!!.rangeInElement.contains(0)
        )
        val resolved = reference.resolve()
        assertInstanceOf(resolved, PSIdentifierImpl::class.java)
        assertEquals("x", (resolved as PSIdentifierImpl?)!!.name)
    }
}