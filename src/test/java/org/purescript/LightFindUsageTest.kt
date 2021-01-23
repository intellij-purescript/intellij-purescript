package org.purescript

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.purescript.file.PSFile

class LightFindUsageTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun `test find usage top level`() {
        val main = myFixture.configureByFile("Main.purs") as PSFile
        val fn = main.topLevelValueDeclarations["fn"]!!.first()
        val usages = myFixture.findUsages(fn)
        assertNotEmpty(usages)
    }

    fun `test find usage simple parameter`() {
        val main = myFixture.configureByFile("Main.purs") as PSFile
        val fn = main.topLevelValueDeclarations["fn"]!!.first()
        val x = fn.varBindersInParameters["x"]!!
        val usages = myFixture.findUsages(x)
        assertNotEmpty(usages)
    }
}