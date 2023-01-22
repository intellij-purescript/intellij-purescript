package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.util.PsiEditorUtilBase
import org.purescript.file.PSFile
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedItem
import org.purescript.psi.base.PSPsiElement
import java.util.*

class ImportQuickFix(vararg val imports: ImportDeclaration) : LocalQuickFix {

    override fun getFamilyName(): String = "Import"

    override fun getName(): String =
        when (val import = imports.singleOrNull()) {
            null -> "Import"
            else -> import.toString().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val hostModule = (descriptor.psiElement as? PSPsiElement)?.module
            ?: return
        val import = imports.singleOrNull()
        if (import != null) {
            hostModule.addImportDeclaration(import)
        } else {
            val editor: Editor = PsiEditorUtilBase
                .findEditorByPsiElement(descriptor.psiElement) ?: return
            val sortedImports = 
                imports.toSet().sortedBy { it.moduleName.length }
            val (first, then)  = sortedImports.partition { 
                it.alias != null && it.alias in it.moduleName
            }            
            JBPopupFactory
                .getInstance()
                .createPopupChooserBuilder(first + then)
                .setNamerForFiltering { it.moduleName }
                .setTitle("Imports")
                .setItemChosenCallback {
                    executeCommand(project, "Import") {
                        runWriteAction {
                            hostModule.addImportDeclaration(it)
                        }
                    }
                }
                .createPopup()
                .showInBestPositionFor(editor)
        }
    }

    companion object {
        fun allCombinations(
            moduleName: String,
            alias: String? = null,
            item: ImportedItem
        ): Sequence<ImportQuickFix> = sequence {
            val importWithItem = ImportDeclaration(
                moduleName,
                alias = alias,
                importedItems = setOf(item)
            )
            yield(ImportQuickFix(importWithItem))
            yield(ImportQuickFix(ImportDeclaration(moduleName, alias = alias)))
        }
    }
}
