package org.purescript.psi.typeconstructor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.PSPsiElement

class TypeConstructorReference(typeConstructor: PSTypeConstructor) :
    PsiReferenceBase<PSTypeConstructor>(
        typeConstructor,
        typeConstructor.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toTypedArray()

    override fun resolve(): PsiElement? =
        candidates.firstOrNull { it.name == myElement.name }

    /*
     * TODO [simonolander]
     *  Add support for type declarations
     */
    private val candidates: List<PSPsiElement>
        get() = myElement.module.run {
            dataDeclarations.toList() + newTypeDeclarations.toList() +
                importDeclarations.flatMap { it.importedDataDeclarations + it.importedNewTypeDeclarations }
        }
}
