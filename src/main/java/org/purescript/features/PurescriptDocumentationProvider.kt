package org.purescript.features

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import org.purescript.psi.PSModule

class PurescriptDocumentationProvider: AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        return when (element) {
            is PSModule -> element.getDocString()
            else -> null
        }
    }
}
