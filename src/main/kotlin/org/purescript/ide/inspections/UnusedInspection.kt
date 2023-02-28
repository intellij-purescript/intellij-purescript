package org.purescript.ide.inspections

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch.search
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.safeDelete.SafeDeleteHandler
import org.purescript.psi.declaration.classes.PSInstanceDeclaration
import org.purescript.psi.declaration.imports.Import
import org.purescript.psi.declaration.imports.PSImportedData
import org.purescript.psi.declaration.imports.PSImportedDataMember
import org.purescript.psi.declaration.imports.PSImportedValue
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.declaration.value.ValueDeclarationGroup

class UnusedInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        Visitor(holder)

    class Visitor(private val holder: ProblemsHolder) : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) = when (element) {
            is ValueDeclarationGroup -> when {
                element.name == "main" -> Unit
                element.parent is PSInstanceDeclaration -> Unit
                search(element).anyMatch { it.element !is PSSignature } -> Unit
                else -> holder.registerProblem(
                    element.nameIdentifier,
                    "Unused value declaration",
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    SafeDelete(element)
                )
            }

            is PSImportedValue -> {
                if (element.parentOfType<Import>()?.isExported == true) Unit
                else {
                    if (referenceIsUsedInFile(element)) Unit
                    else holder.registerProblem(
                        element.identifier,
                        "Unused imported value",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        SafeDelete(element)
                    )
                }
            }

            is PSImportedDataMember -> {
                if (element.parentOfType<Import>()?.isExported == true) Unit
                else {
                    if (referenceIsUsedInFile(element)) Unit
                    else holder.registerProblem(
                        element,
                        "Unused imported data constructor",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        SafeDelete(element)
                    )
                }
            }
            is PSImportedData -> {
                if (element.parentOfType<Import>()?.isExported == true) Unit
                else when {
                    referenceIsUsedInFile(element) -> Unit
                    element.importedDataMembers.any { referenceIsUsedInFile(it)} -> Unit
                    else -> holder.registerProblem(
                        element,
                        "Unused imported data",
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        SafeDelete(element)
                    )
                }
            }
            else -> Unit
        }

        private inline fun <reified E : PsiElement> referenceIsUsedInFile(element: E): Boolean {
            val reference = element.reference?.resolve()
            val scope = GlobalSearchScope.fileScope(element.containingFile)
            return reference == null || search(reference, scope, true).anyMatch { it.element !is E }
        }

    }

    class SafeDelete(element: PsiElement) : LocalQuickFixOnPsiElement(element) {
        override fun getFamilyName() = "Safe delete"
        override fun getText(): String = "Safe delete"
        override fun getFileModifierForPreview(target: PsiFile): FileModifier? = null

        override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) =
            safeDelete(project, startElement)

        private fun safeDelete(project: Project, element: PsiElement) {
            ApplicationManager.getApplication().invokeLater(
                { SafeDeleteHandler.invoke(project, arrayOf(element), false) },
                ModalityState.NON_MODAL
            )
        }
    }
}