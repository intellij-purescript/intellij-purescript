package org.purescript.ide.inspections

import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.searches.ReferencesSearch.search
import com.intellij.refactoring.safeDelete.SafeDeleteHandler
import org.purescript.psi.declaration.PSSignature
import org.purescript.psi.declaration.PSValueDeclaration

class UnusedInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) = when (element) {
            is PSValueDeclaration -> when {
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

    class SafeDelete(private val element: PsiElement) : LocalQuickFix {
        override fun getFamilyName() = "Safe delete"
        override fun applyFix(project: Project, descriptor: ProblemDescriptor) =
            ApplicationManager.getApplication().invokeLater(
                { SafeDeleteHandler.invoke(project, arrayOf(element), false) },
                ModalityState.NON_MODAL
            )
    }
}