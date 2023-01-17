package org.purescript.features

import com.intellij.codeInsight.daemon.ReferenceImporter
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.elementsAtOffsetUp
import org.purescript.file.PSFile
import org.purescript.psi.declaration.value.ExportedValueDeclNameIndex
import org.purescript.psi.expression.ExpressionIdentifierReference
import org.purescript.psi.expression.PSExpressionIdentifier
import java.util.function.BooleanSupplier

class PSReferenceImporter: ReferenceImporter {
    override fun isAddUnambiguousImportsOnTheFlyEnabled(file: PsiFile): Boolean {
        return file is PSFile.Psi
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
        val toImport = index.get(element.name, element.project, scope)
            .singleOrNull()
            ?.asImport() ?: return@BooleanSupplier false
        WriteAction.run<RuntimeException> {
            CommandProcessor.getInstance().runUndoTransparentAction {
                module.addImportDeclaration(toImport.withAlias(element.qualifierName))
            }
        }
        true
    }
}