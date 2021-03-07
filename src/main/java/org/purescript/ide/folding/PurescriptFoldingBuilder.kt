package org.purescript.ide.folding

import com.intellij.codeInsight.folding.CodeFoldingSettings
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.file.PSFile
import org.purescript.parser.PSElements

class PurescriptFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (root !is PSFile) return emptyArray()

        val visitor = PurescriptFoldingVisitor()
        PsiTreeUtil.processElements(root) {
            it.accept(visitor)
            true
        }
        return visitor.descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return with(CodeFoldingSettings.getInstance()) {
            when (node.elementType) {
                PSElements.ImportDeclaration -> COLLAPSE_IMPORTS
                else -> false
            }
        }
    }
}
