package org.purescript.parser

class InstanceParserTest: PSLanguageParserTestBase("instance") {
    fun testShowBoolean() = doTest(true, true)
    fun testShowInstanceWithoutName() = doTest(true, true)
}