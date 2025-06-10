package org.purescript.features

import com.intellij.codeInsight.daemon.ReferenceImporter
import com.intellij.codeInsight.daemon.impl.ShowAutoImportPass
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.elementsAtOffsetUp
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.purescript.file.PSFile
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.value.binder.ConstructorBinder
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.Qualified
import java.util.function.BooleanSupplier

class PSReferenceImporter : ReferenceImporter {
    override fun isAddUnambiguousImportsOnTheFlyEnabled(file: PsiFile): Boolean = file is PSFile

    @Suppress("UnstableApiUsage")
    override fun computeAutoImportAtOffset(
        editor: Editor,
        file: PsiFile,
        offset: Int,
        allowCaretNearReference: Boolean
    ) = BooleanSupplier {
        val element = file.elementsAtOffsetUp(offset)
            .asSequence()
            .map { it.first }
            .filterIsInstance<Qualified>()
            .firstOrNull() ?: return@BooleanSupplier false
        if ((element as? PsiElement)?.reference?.resolve() != null) return@BooleanSupplier false
        val module = (file as? PSFile)?.module ?: return@BooleanSupplier false
        val project = element.project
        val scope = GlobalSearchScope.allScope(project)
        val name = element.getName()
        val possibleImports = when(element) {
            is ExpressionAtom -> StubIndex.getElements(ImportableIndex.getKey(), name, project, scope, Importable::class.java)
            is ConstructorBinder -> StubIndex.getElements(ImportableIndex.getKey(), name, project, scope, Importable::class.java)
            else -> StubIndex.getElements(ImportableTypeIndex.getKey(), name, project, scope, Importable::class.java)
        } .mapNotNull { it.asImport() }.map { it.withAlias(element.qualifierName) }.toList()
        val qualifiedImports: List<String> = module.cache.importsByName
            .getOrDefault(element.qualifierName, listOf())
            .map { it.moduleNameName }
        val possibleQualifiedImports = possibleImports.filter { it.moduleName in qualifiedImports }
        val possibleAlreadyImported = possibleImports
            .filter {
                it.alias == null &&
                        it.moduleName in module.cache.importsByModule &&
                        module.cache.importsByModule
                            .get(it.moduleName)!!
                            .any { import ->
                                import.importAlias == null
                            }
            }

        val hintManager = HintManager.getInstance()
        when {
            possibleImports.isEmpty() -> false
            possibleImports.size == 1 -> invoke(
                possibleImports.single(),
                hintManager,
                editor,
                element,
                module
            )

            possibleQualifiedImports.size == 1 -> invoke(
                possibleQualifiedImports.single(),
                hintManager,
                editor,
                element,
                module
            )

            possibleAlreadyImported.size == 1 -> invoke(
                possibleAlreadyImported.single(),
                hintManager,
                editor,
                element,
                module
            )

            else -> false
        }
    }

    private fun invoke(
        toImport: ImportDeclaration,
        hintManager: HintManager,
        editor: Editor,
        element: PsiElement,
        module: org.purescript.module.Module
    ): Boolean {
        val message = ShowAutoImportPass.getMessage(false, "$toImport")
        hintManager.showQuestionHint(
            editor,
            message,
            element.startOffset,
            element.endOffset
        ) {
            import(module, toImport)
        }
        return true
    }

    fun import(module: org.purescript.module.Module, toImport: ImportDeclaration): Boolean {
        WriteAction.run<RuntimeException> {
            CommandProcessor.getInstance().runUndoTransparentAction {
                module.addImportDeclaration(toImport)
            }
        }
        return true
    }
}