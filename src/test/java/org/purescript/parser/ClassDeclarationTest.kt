package org.purescript.parser

class ClassDeclarationTest: PSLanguageParserTestBase("class") {
    fun testWithConstraint() = doTest(true, true)
}