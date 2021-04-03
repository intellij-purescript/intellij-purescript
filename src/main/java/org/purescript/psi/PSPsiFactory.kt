package org.purescript.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.util.findDescendantOfType
import org.purescript.PSLanguage
import org.purescript.psi.imports.PSImportDeclarationImpl

class PSPsiFactory(private val project: Project) {

    fun createQualifiedProperName(name: String): PSProperName? =
        createFromText("module $name where")

    fun createImportDeclaration(moduleName: String): PSImportDeclarationImpl? =
        createFromText(
            """
                module Foo where
                import $moduleName
            """.trimIndent()
        )

    fun createNewLine(): PsiElement =
        PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n")

    private inline fun <reified T : PsiElement> createFromText(code: String): T? =
        PsiFileFactory.getInstance(project)
            .createFileFromText(PSLanguage.INSTANCE, code)
            .findDescendantOfType()
}
