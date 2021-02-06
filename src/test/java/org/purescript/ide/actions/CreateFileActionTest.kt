package org.purescript.ide.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * The method called for creating the file, [com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog.create],
 * actually changes the name of the file when running as a unit test. Files are always called `Purescript Module.purs`
 */
const val fileName = "Purescript Module.purs"

class CreateFileActionTest : BasePlatformTestCase() {
    fun `test create simple purescript file`() {
        performCreateFileAction("Foo")
        myFixture.checkResult(
            fileName,
            getExpectedModuleBody("Foo"),
            false
        )
    }

    fun `test strips purs suffix`() {
        performCreateFileAction("Foo.purs")
        myFixture.checkResult(
            fileName,
            getExpectedModuleBody("Foo"),
            false
        )
    }

    fun `test create file in src directory`() {
        performCreateFileAction("Bar", "src")
        myFixture.checkResult(
            "src/$fileName",
            getExpectedModuleBody("Bar"),
            false
        )
    }

    fun `test create file in test directory`() {
        performCreateFileAction("Bar", "test")
        myFixture.checkResult(
            "test/$fileName",
            getExpectedModuleBody("Bar"),
            false
        )
    }

    fun `test create file in nested directory`() {
        performCreateFileAction("Bar", "src/Foo")
        myFixture.checkResult(
            "src/Foo/$fileName",
            getExpectedModuleBody("Foo.Bar"),
            false
        )
    }

    fun `test removes purs suffix in nested directory`() {
        performCreateFileAction("Bar.purs", "src/Foo")
        myFixture.checkResult(
            "src/Foo/$fileName",
            getExpectedModuleBody("Foo.Bar"),
            false
        )
    }

    fun `test create file with full name in nested directory`() {
        performCreateFileAction("Foo.Bar", "src/Foo")
        myFixture.checkResult(
            "src/Foo/$fileName",
            getExpectedModuleBody("Foo.Bar"),
            false
        )
    }

    private fun getExpectedModuleBody(moduleName: String): String {
        return """
            module $moduleName where

            import Prelude

            """.trimIndent()
    }

    private fun performCreateFileAction(name: String, directoryName: String = ""): PsiFile {
        val file = myFixture.tempDirFixture.findOrCreateDir(directoryName)
        val directory = myFixture.psiManager.findDirectory(file)!!
        val action = ActionManager.getInstance().getAction("Purescript.NewFile") as CreateFileAction
        return action.internalCreateFile(name, "Purescript Module", directory)!!
    }

}
