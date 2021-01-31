package org.purescript.features

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.psi.PSValueDeclaration
import org.jetbrains.annotations.Nls
import org.purescript.file.PSFile
import org.purescript.psi.PSModule
import org.purescript.psi.PSVarBinderImpl

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return when (psiElement) {
            is PSValueDeclaration, is PSVarBinderImpl, is PSModule -> {
                true
            }
            else -> {
                false
            }
        }
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): @Nls String {
        return when (element) {
            is PSValueDeclaration -> "value"
            is PSVarBinderImpl -> "parameter"
            is PSModule -> "module"
            else -> "unknown"
        }
    }

    override fun getDescriptiveName(element: PsiElement): @Nls String {
        when (element) {
            is PSValueDeclaration -> {
                val file = element.containingFile as PSFile
                return "${file.module.name}.${element.name}"
            }
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
        if (useFullName) {
            return getDescriptiveName(element)
        } else if (element is PsiNamedElement) {
            val name = element.name
            if (name != null) {
                return name
            }
        }
        return ""
    }
}