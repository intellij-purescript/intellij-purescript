package org.purescript.parser

class RoleParserTest: PSLanguageParserTestBase("role") {
    fun testRepresentational () = doTest(true, true)
}