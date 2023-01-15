package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.searches.ReferencesSearch.search
import com.intellij.refactoring.safeDelete.SafeDeleteHandler
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.declaration.value.ValueDecl

class UnusedInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) = when (element) {
            is ValueDecl.Psi -> when {
                element.name == "main" -> Unit
                search(element).anyMatch { it.element !is PSSignature } -> Unit
                else -> holder.registerProblem(
                    element.nameIdentifier,
                    "Unused value declaration",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    SafeDelete(element)
                )
            }

            else -> Unit
        }

    }

    class SafeDelete(element: PsiElement) : LocalQuickFixOnPsiElement(element) {
        override fun getFamilyName() = "Safe delete"
        override fun getText(): String = "Safe delete"

        override fun invoke(
            project: Project,
            file: PsiFile,
            startElement: PsiElement,
            endElement: PsiElement
        ) {
            safeDelete(project, startElement)
        }


        private fun safeDelete(project: Project, element: PsiElement) {
            ApplicationManager.getApplication().invokeLater(
                { SafeDeleteHandler.invoke(project, arrayOf(element), false) },
                ModalityState.NON_MODAL
            )
        }
    }
}