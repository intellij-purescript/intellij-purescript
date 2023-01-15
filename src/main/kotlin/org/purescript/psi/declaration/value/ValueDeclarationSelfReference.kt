package org.purescript.psi.declaration.value

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiFactory

class ValueDeclarationSelfReference(valueDeclaration: ValueDecl.Psi) :
    PsiReferenceBase<ValueDecl.Psi>(
        valueDeclaration,
        valueDeclaration.nameIdentifier.textRangeInParent,
        false
    ) {

    override fun resolve(): PsiElement? {
        return element.module.cache.valueDeclarations
            .firstOrNull { it.name == element.name }
    }

    override fun handleElementRename(name: String): PsiElement? {
        val oldName = element.nameIdentifier
        val newName = PSPsiFactory(element.project).createIdentifier(name)
            ?: return null
        oldName.replace(newName)
        return element
    }
}
