package org.purescript.parser

class CaseOfParserTest: PSLanguageParserTestBase("case-of") {
    fun testWithQualifiedOperator() = doTest(true, true)
}