package org.purescript.parser

class TypeParserTest: PSLanguageParserTestBase("type") {
    fun testNewTypeWithRowTypeType() = doTest(true, true)
    fun testPolyKinds() = doTest(true, true)
    fun testPolyNewKinds() = doTest(true, true)
    fun testRecordRowNamedType() = doTest(true, true)
    fun testTypeLevelInt() = doTest(true, true)
}