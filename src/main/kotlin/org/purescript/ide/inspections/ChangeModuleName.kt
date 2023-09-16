package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.purescript.module.Module

class ChangeModuleName(module: Module, private val name: String) 
    : LocalQuickFixOnPsiElement(module) {
    override fun getFamilyName() = "Fix Module Name"
    override fun getText() = "Change name to '$name'"
    override fun invoke(
        project: Project, file: PsiFile, start: PsiElement, end: PsiElement
    ) {
        val module = (start as? Module)
        module?.setName(name)
    }
}