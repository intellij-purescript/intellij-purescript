package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import org.purescript.psi.PSModule
import org.purescript.psi.PSValueDeclaration

class PurescriptDocumentationProvider: AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is PSModule -> element.getDocString()
            is PSValueDeclaration -> element.documentation
            else -> null
        }
    }
}
