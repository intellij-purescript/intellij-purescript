package org.purescript.ide.folding

import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import org.purescript.psi.exports.PSExportList
import org.purescript.psi.PSModule

class PurescriptFoldingVisitor : PsiElementVisitor() {
    val descriptors: ArrayList<FoldingDescriptor> = ArrayList()

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        when (element) {
            is PSModule -> visitModule(element)
            is PSExportList -> visitExportList(element)
        }
    }

    private fun visitModule(module: PSModule) {
        val importDeclarations = module.importDeclarations
        if (importDeclarations.size < 2) {
            return
        }
        val firstImportDeclaration = importDeclarations.first()
        val startOffset = firstImportDeclaration.importName?.startOffset
            ?: return
        val endOffset = importDeclarations.last().endOffset
        val descriptor = FoldingDescriptor(firstImportDeclaration, TextRange(startOffset, endOffset))
        descriptors += descriptor
    }

    private fun visitExportList(exportList: PSExportList) {
        val exportedItems = exportList.exportedItems
        val startOffset = exportList.startOffset + 1
        val endOffset = exportList.endOffset - 1
        if (startOffset >= endOffset) {
            return
        }
        val descriptor = FoldingDescriptor(exportList, TextRange(startOffset, endOffset))
        descriptors += descriptor
    }
}
