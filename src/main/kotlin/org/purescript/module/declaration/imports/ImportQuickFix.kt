package org.purescript.module.declaration.imports

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.util.PsiEditorUtilBase
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.psi.PSPsiElement
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
            val editor: Editor = PsiEditorUtilBase.findEditorByPsiElement(descriptor.psiElement) ?: return
            val sortedImports = imports.toSet().sortedBy { it.moduleName.length }
            val (first, then)  = sortedImports.partition { 
                it.alias != null && it.alias in it.moduleName
            }

            val renderer = DefaultPsiElementCellRenderer()
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
                .setRenderer (renderer::getListCellRendererComponent)
                .createPopup()
                .showInBestPositionFor(editor)
        }
    }
}
