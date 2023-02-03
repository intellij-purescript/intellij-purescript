package org.purescript.ide.inspections

import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

fun visitFile(block: PsiFile.() -> Unit): PsiElementVisitor {
    return object: PsiElementVisitor() {
        override fun visitFile(file: PsiFile) {
            file.block()
        }
    }
}