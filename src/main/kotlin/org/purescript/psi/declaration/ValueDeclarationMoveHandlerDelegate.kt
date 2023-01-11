package org.purescript.psi.declaration

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.move.MoveHandler
import com.intellij.refactoring.move.MoveHandlerDelegate
import com.intellij.refactoring.ui.RefactoringDialog
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import org.purescript.PSLanguage
import org.purescript.psi.module.Module
import org.purescript.psi.module.ModuleNameIndex

class ValueDeclarationMoveHandlerDelegate : MoveHandlerDelegate() {
    override fun supportsLanguage(language: Language) = language == PSLanguage
    override fun canMove(
        elements: Array<out PsiElement>,
        targetContainer: PsiElement?,
        reference: PsiReference?
    ): Boolean {
        return reference == null &&
            elements.size == 1 &&
            elements.first() is PSValueDeclaration
    }

    override fun tryToMove(
        element: PsiElement,
        project: Project,
        dataContext: DataContext?,
        reference: PsiReference?,
        editor: Editor?
    ): Boolean = when (element) {
        is PSValueDeclaration -> {
            Dialog(element, project).show()
            true
        }

        else -> false
    }

    class Dialog(val element: PSValueDeclaration, project: Project) :
        RefactoringDialog(project, false) {

        private var targetModuleName: String
        private val moduleNameIndex: ModuleNameIndex

        override fun createCenterPanel() = panel {
            row("To:") {
                val modules = moduleNameIndex
                    .getAllKeys(project)
                    .sorted()
                comboBox(modules)
                    .bindItem(
                        { targetModuleName },
                        { targetModuleName = it ?: "" }
                    )
            }
        }

        override fun doAction() {
            applyFields()
            val modules: MutableCollection<Module.Psi> = moduleNameIndex.get(
                targetModuleName,
                project,
                GlobalSearchScope.allScope(project)
            )
            val targetModule = modules.single()
            invokeRefactoring(object: BaseRefactoringProcessor(project) {
                override fun createUsageViewDescriptor(usages: Array<out UsageInfo>): UsageViewDescriptor {
                    return BaseUsageViewDescriptor(element)
                }

                override fun findUsages(): Array<UsageInfo>  =
                    ReferencesSearch
                    .search(element, GlobalSearchScope.projectScope(project))
                    .findAll()
                    .map(::UsageInfo)
                    .toTypedArray()

                override fun performRefactoring(usages: Array<out UsageInfo>) {
                    val first = element.signature ?: element
                    targetModule.addRange(first, element)
                    element.module?.deleteChildRange(first, element)
                }

                override fun getCommandName(): String = MoveHandler.getRefactoringName()

            })
        }

        init {
            title = MoveHandler.getRefactoringName()
            this.targetModuleName = element.module!!.name
            this.moduleNameIndex = ModuleNameIndex()
            super.init()
        }
    }
}