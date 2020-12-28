package org.purescript.features

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.psi.PSIdentifierImpl
import org.purescript.psi.PSValueDeclarationImpl
import org.jetbrains.annotations.Nls

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return (psiElement is PSValueDeclarationImpl
                || psiElement is PSIdentifierImpl)
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): @Nls String {
        if (element is PSValueDeclarationImpl) {
            return "value"
        } else if (element is PSIdentifierImpl) {
            return "parameter"
        }
        return "unknown"
    }

    override fun getDescriptiveName(element: PsiElement): @Nls String {
        when (element) {
            is PsiNamedElement -> {
                val name = element.name
                if (name != null) {
                    return name
                }
            }
        }
        return ""
    }

    override fun getNodeText(
        element: PsiElement,
        useFullName: Boolean
    ): @Nls String {
        if (element is PsiNamedElement) {
            val name = element.name
            if (name != null) {
                return name
            }
        }
        return ""
    }
}