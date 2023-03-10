package org.purescript

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiFile
import org.purescript.file.PSFile


class PSStructureViewBuilder: PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        val root = TreeElement(psiFile)
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?) = StructureViewModelBase(psiFile, editor, root)

        }
    }

    class TreeElement(val element: NavigatablePsiElement) : StructureViewTreeElement {
        override fun getPresentation(): ItemPresentation {
            return element.presentation ?: PresentationData()
        }

        override fun getChildren(): Array<TreeElement> = when (element) {
            is PSFile -> element.module?.let { arrayOf(TreeElement(it)) } ?: arrayOf()
            is org.purescript.module.Module -> element.cache.valueDeclarationGroups.map { TreeElement(it) }.toTypedArray()
            else -> arrayOf()
        }

        override fun navigate(requestFocus: Boolean) = element.navigate(requestFocus)
        override fun canNavigate(): Boolean = element.canNavigate()
        override fun canNavigateToSource(): Boolean = element.canNavigateToSource()
        override fun getValue(): Any = element

    }
}