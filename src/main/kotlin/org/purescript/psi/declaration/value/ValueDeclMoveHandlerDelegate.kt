package org.purescript.psi.declaration.value

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.move.MoveHandler
import com.intellij.refactoring.move.MoveHandlerDelegate
import com.intellij.refactoring.ui.RefactoringDialog
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import org.purescript.PSLanguage
import org.purescript.psi.module.Module
import org.purescript.psi.module.ModuleNameIndex

class ValueDeclMoveHandlerDelegate : MoveHandlerDelegate() {
    override fun supportsLanguage(language: Language) = language == PSLanguage
    override fun canMove(
        elements: Array<out PsiElement>,
        targetContainer: PsiElement?,
        reference: PsiReference?
    ): Boolean {
        return reference == null &&
            elements.size == 1 &&
            elements.first() is ValueDeclarationGroup
    }

    override fun tryToMove(
        element: PsiElement,
        project: Project,
        dataContext: DataContext?,
        reference: PsiReference?,
        editor: Editor?
    ): Boolean = when (element) {
        is ValueDeclarationGroup -> {
            Dialog(element, project).show()
            true
        }

        else -> false
    }

    class Dialog(val element: ValueDeclarationGroup, project: Project) :
        RefactoringDialog(project, false) {

        private var targetModuleName: String
        private val moduleNameIndex: ModuleNameIndex

        override fun createCenterPanel() = panel {
            row("To:") {
                val modules = moduleNameIndex
                    .getAllKeys(GlobalSearchScope.projectScope(project))
                    .sorted()
                comboBox(modules)
                    .focused()
                    .bindItem(
                        { targetModuleName },
                        { targetModuleName = it ?: "" }
                    )
            }
        }

        override fun doAction() {
            applyFields()
            val modules: MutableCollection<Module> = moduleNameIndex.get(
                targetModuleName,
                project,
                GlobalSearchScope.projectScope(project)
            )
            val targetModule = modules
                .single { ".spago" !in it.containingFile.virtualFile.path }
            invokeRefactoring(
                MoveValueDeclRefactoring(element, targetModule)
            )
        }

        init {
            title = MoveHandler.getRefactoringName()
            this.targetModuleName = element.module!!.name
            this.moduleNameIndex = ModuleNameIndex()
            super.init()
        }
    }
}

