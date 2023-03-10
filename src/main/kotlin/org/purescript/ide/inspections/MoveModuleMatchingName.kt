package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.purescript.module.Module

class MoveModuleMatchingName(private val module: Module) : LocalQuickFixOnPsiElement(module) {
    override fun getFamilyName() = "Move module"
    override fun getText() = "Move module to '${module.name.replace('.', '/') + ".purs"}'"
    override fun invoke(
        project: Project, file: PsiFile, start: PsiElement, end: PsiElement
    ) {
        val module = (start as? Module)?.name ?: return
        var src = file.parent
        while (src != null && src.name.first().isUpperCase()) {
            src = src.parent
        }
        val basePath = src?.virtualFile ?: return
        var newPath = basePath
        val names = module.split(".")
        for (name in names.dropLast(1)) {
            newPath = newPath.findChild(name) ?:
                newPath.createChildDirectory(this, name)
        }
        runWriteAction {
            file.virtualFile.rename(this, names.last() + ".purs")
            file.virtualFile.move(this, newPath)
        }
    }
}