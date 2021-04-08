package org.purescript.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.purescript.PSLanguage
import org.purescript.psi.imports.PSImportDeclarationImpl
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSProperName


/**
 * This should be [com.intellij.psi.util.findDescendantOfType]
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.findDescendantOfType(noinline predicate: (T) -> Boolean = { true }): T? {
    return findDescendantOfType({ true }, predicate)
}
/**
 * This should be [com.intellij.psi.util.findDescendantOfType]
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.findDescendantOfType(
    crossinline canGoInside: (PsiElement) -> Boolean,
    noinline predicate: (T) -> Boolean = { true }
): T? {
    var result: T? = null
    this.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is T && predicate(element)) {
                result = element
                stopWalking()
                return
            }

            if (canGoInside(element)) {
                super.visitElement(element)
            }
        }
    })
    return result
}

class PSPsiFactory(private val project: Project) {

    fun createModuleName(name: String): PSModuleName? =
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
