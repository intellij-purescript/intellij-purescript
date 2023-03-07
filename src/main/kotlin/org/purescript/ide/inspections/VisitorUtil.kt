package org.purescript.ide.inspections

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

fun visitFile(block: PsiFile.() -> Unit): PsiElementVisitor {
    return object: PsiElementVisitor() {
        override fun visitFile(file: PsiFile) {
            file.block()
        }
    }
}

fun visitElement(block: PsiElement.() -> Unit): PsiElementVisitor {
    return object: PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            element.block()
        }
    }
}