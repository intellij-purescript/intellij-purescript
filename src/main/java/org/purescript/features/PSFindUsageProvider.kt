package org.purescript.features

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.file.PSFile
import org.purescript.psi.*
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.imports.PSImportAlias
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is PSValueDeclaration
            || psiElement is PSVarBinderImpl
            || psiElement is PSModule
            || psiElement is PSForeignValueDeclaration
            || psiElement is PSNewTypeDeclarationImpl
            || psiElement is PSImportAlias
            || psiElement is PSDataDeclaration
            || psiElement is PSDataConstructor
            || psiElement is PSClassDeclaration
            || psiElement is PSTypeSynonymDeclaration

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return when (element) {
            is PSValueDeclaration -> "value"
            is PSVarBinderImpl -> "parameter"
            is PSModule -> "module"
            is PSNewTypeDeclarationImpl -> "newtype"
            is PSImportAlias -> "import alias"
            is PSDataDeclaration -> "data"
            is PSDataConstructor -> "data constructor"
            is PSClassDeclaration -> "class"
            else -> "unknown"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
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
    ): String {
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
