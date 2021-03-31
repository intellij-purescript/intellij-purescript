package org.purescript.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.findDescendantOfType
import org.purescript.PSLanguage

class PSPsiFactory(private val project: Project) {

    fun createQualifiedProperName(name: String): PSProperName? =
        createFromText("module $name where")

    private inline fun <reified T : PsiElement> createFromText(code: String): T? =
        PsiFileFactory.getInstance(project)
            .createFileFromText(PSLanguage.INSTANCE, code)
            .findDescendantOfType()
}
