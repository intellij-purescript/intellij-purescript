package org.purescript.ide.folding

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.purescript.module.Module
import org.purescript.module.exports.ExportList

class PurescriptFoldingVisitor : PsiElementVisitor() {
    val descriptors: ArrayList<FoldingDescriptor> = ArrayList()

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        when (element) {
            is Module -> visitModule(element)
            is ExportList -> visitExportList(element)
        }
    }

    private fun visitModule(module: Module) {
        val importDeclarations = module.cache.imports
        if (importDeclarations.size < 2) {
            return
        }
        val firstImportDeclaration = importDeclarations.first()
        val startOffset = firstImportDeclaration.moduleName.startOffset
        val endOffset = importDeclarations.last().endOffset
        val descriptor = FoldingDescriptor(
            firstImportDeclaration,
            TextRange(startOffset, endOffset)
        )
        descriptors += descriptor
    }

    private fun visitExportList(exportList: ExportList) {
        val startOffset = exportList.startOffset + 1
        val endOffset = exportList.endOffset - 1
        if (startOffset >= endOffset) {
            return
        }
        val descriptor = FoldingDescriptor(exportList, TextRange(startOffset, endOffset))
        descriptors += descriptor
    }
}
