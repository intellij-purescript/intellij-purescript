package org.purescript.features

import com.intellij.codeInsight.daemon.ReferenceImporter
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.elementsAtOffsetUp
import org.purescript.file.PSFile
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.psi.declaration.value.ExportedValueDeclNameIndex
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.module.Module
import java.util.function.BooleanSupplier

class PSReferenceImporter: ReferenceImporter {
    override fun isAddUnambiguousImportsOnTheFlyEnabled(file: PsiFile): Boolean {
        return file is PSFile.Psi && false
    }

    @Suppress("UnstableApiUsage")
    override fun computeAutoImportAtOffset(
        editor: Editor, file: PsiFile, offset: Int, allowCaretNearReference: Boolean
    ) = BooleanSupplier { 
        val element = file.elementsAtOffsetUp(offset)
            .asSequence()
            .map { it.first }
            .filterIsInstance<PSExpressionIdentifier>()
            .firstOrNull() ?: return@BooleanSupplier false
        if(element.reference.resolve() != null) return@BooleanSupplier false
        val module = (file as? PSFile.Psi)?.module ?: return@BooleanSupplier false
        val scope = GlobalSearchScope.allScope(element.project)
        val index = ExportedValueDeclNameIndex()
        val possibleImports = index.get(element.name, element.project, scope)
            .mapNotNull { it.asImport() }
            .map { it.withAlias(element.qualifierName) }
            .toList()
        val qualifiedImports: List<String> = module.cache.importsByName
            .getOrDefault(element.qualifierName, listOf())
            .map { it.moduleName.name }
        val possibleQualifiedImports = possibleImports
            .filter { it.moduleName in qualifiedImports }
        val possibleAlreadyImported = possibleImports
            .filter {
                it.alias == null && 
                it.moduleName in module.cache.importsByModule &&
                    module.cache.importsByModule.get(it.moduleName)!!.any { import ->
                        import.importAlias == null
                    }
            }
        
        when {
            possibleImports.isEmpty() -> false
            possibleImports.size == 1 ->
                import(module, possibleImports.single())
            possibleQualifiedImports.size == 1 ->
                import(module, possibleQualifiedImports.single())
            possibleAlreadyImported.size == 1 ->
                import(module, possibleAlreadyImported.single())
            else -> false
        }
    }
    
    fun import(module: Module.Psi,toImport: ImportDeclaration): Boolean {
        WriteAction.run<RuntimeException> {
            CommandProcessor.getInstance().runUndoTransparentAction {
                module.addImportDeclaration(toImport)
            }
        }
        return true
    }
}