package org.purescript.ide.inspections

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemHighlightType.LIKE_UNUSED_SYMBOL
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch.search
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.refactoring.safeDelete.SafeDeleteHandler
import org.purescript.parser.COMMA
import org.purescript.parser.PSParserDefinition
import org.purescript.psi.declaration.classes.PSInstanceDeclaration
import org.purescript.psi.declaration.imports.*
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
                    getDescription(element),
                    LIKE_UNUSED_SYMBOL,
                    SafeDelete(element)
                )
            }

            is PSImportedValue , is PSImportedOperator, is PSImportedDataMember -> when {
                element.parentOfType<Import>()?.isExported == true -> Unit
                referenceIsUsedInFile(element) -> Unit
                else -> registerImportItem(element)
            }

            is PSImportedData -> when {
                element.parentOfType<Import>()?.isExported == true -> Unit
                referenceIsUsedInFile(element) -> Unit
                element.importedDataMembers.any { referenceIsUsedInFile(it) } -> Unit
                else -> registerImportItem(element)
            }

            else -> Unit
        }

        private fun registerImportItem(element: PsiElement) =
            holder.registerProblem(element, getDescription(element), LIKE_UNUSED_SYMBOL, UnusedImport(element))

        private fun getDescription(element: PsiElement): String = when (element) {
            is ValueDeclarationGroup -> "Unused value declaration"
            is PSImportedValue -> "Unused imported value"
            is PSImportedOperator -> "Unused imported operator"
            is PSImportedDataMember -> "Unused imported data constructor"
            is PSImportedData -> "Unused imported data"
            else -> ""
        }

        private inline fun <reified E : PsiElement> referenceIsUsedInFile(element: E): Boolean {
            val reference = element.reference?.resolve()
            val scope = GlobalSearchScope.fileScope(element.containingFile)
            return reference == null || search(reference, scope, true).anyMatch { 
                it.element !is PSImportedItem && it.element !is PSImportedDataMember 
            }
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
    
    class UnusedImport(element: PsiElement) : LocalQuickFixOnPsiElement(element) {
        override fun getFamilyName() = "Unused Import"
        override fun getText(): String = "Unused Import"
        override fun getFileModifierForPreview(target: PsiFile): FileModifier? = null

        override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
            val other = when (startElement) {
                        is PSImportedItem -> {
                            val import = startElement.parentOfType<Import>()
                            if ((import?.importedItems?.size == 1)) {
                                listOf(import)
                            } else {
                                val definition = PSParserDefinition()
                                val comma = startElement.siblings(true, false)
                                    .takeWhile {
                                        it.elementType == COMMA ||
                                                definition.whitespaceTokens.contains(it.elementType) ||
                                                definition.commentTokens.contains(it.elementType)
                                    }
                                    .toList()
                                if (comma.any { it.elementType == COMMA }) comma
                                else
                                    comma + startElement.siblings(false, false)
                                        .takeWhile {
                                            it.elementType == COMMA ||
                                                    definition.whitespaceTokens.contains(it.elementType) ||
                                                    definition.commentTokens.contains(it.elementType)
                                        }
                                        .toList()
                            }
                        }

                is PSImportedDataMember -> {
                    val definition = PSParserDefinition()
                    val comma = startElement.siblings(true, false)
                        .takeWhile {
                            it.elementType == COMMA ||
                                    definition.whitespaceTokens.contains(it.elementType) ||
                                    definition.commentTokens.contains(it.elementType)
                        }
                        .toList()
                    if (comma.any { it.elementType == COMMA }) comma
                    else
                        comma + startElement.siblings(false, false)
                            .takeWhile {
                                it.elementType == COMMA ||
                                        definition.whitespaceTokens.contains(it.elementType) ||
                                        definition.commentTokens.contains(it.elementType)
                            }
                            .toList()
                }
        
                        else -> emptyList()
                    }
            startElement.delete()
            other.forEach { it.delete() }
        }

        private fun safeDelete(it: PsiElement) {
            val other = when (it) {
                is PSImportedItem -> {
                    val import = it.parentOfType<Import>()
                    if ((import?.importedItems?.size == 1)) {
                        listOf(import)
                    } else {
                        val definition = PSParserDefinition()
                        val comma = it.siblings(true, false)
                            .takeWhile {
                                it.elementType == COMMA ||
                                        definition.whitespaceTokens.contains(it.elementType) ||
                                        definition.commentTokens.contains(it.elementType)
                            }
                            .toList()
                        if (comma.any { it.elementType == COMMA }) comma
                        else
                            comma + it.siblings(false, false)
                                .takeWhile {
                                    it.elementType == COMMA ||
                                            definition.whitespaceTokens.contains(it.elementType) ||
                                            definition.commentTokens.contains(it.elementType)
                                }
                                .toList()
                    }
                }

                is PSImportedDataMember -> {
                    val definition = PSParserDefinition()
                    val comma = it.siblings(true, false)
                        .takeWhile {
                            it.elementType == COMMA ||
                                    definition.whitespaceTokens.contains(it.elementType) ||
                                    definition.commentTokens.contains(it.elementType)
                        }
                        .toList()
                    if (comma.any { it.elementType == COMMA }) comma
                    else
                        comma + it.siblings(false, false)
                            .takeWhile {
                                it.elementType == COMMA ||
                                        definition.whitespaceTokens.contains(it.elementType) ||
                                        definition.commentTokens.contains(it.elementType)
                            }
                            .toList()
                }

                else -> emptyList()
            }
            it.delete()
            other.forEach { it.delete() }
        }
    }
}