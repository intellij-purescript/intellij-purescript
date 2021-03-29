package org.purescript.psi.typeconstructor

import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

class TypeConstructorReference(typeConstructor: PSTypeConstructor) :
    PsiReferenceBase<PSTypeConstructor>(
        typeConstructor,
        typeConstructor.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<PsiNamedElement> =
        candidates.toTypedArray()

    override fun resolve(): PsiNamedElement? =
        candidates.firstOrNull { it.name == myElement.name }

    /**
     * Type constructors can reference any data, new type, or synonym declaration
     * in the current module or any of the imported modules.
     */
    private val candidates: List<PsiNamedElement>
        get() {
            val module = element.module ?: return emptyList()
            val candidates = mutableListOf<PsiNamedElement>()
            candidates.addAll(module.dataDeclarations)
            candidates.addAll(module.newTypeDeclarations)
            candidates.addAll(module.typeSynonymDeclarations)
            for (importDeclaration in module.importDeclarations) {
                candidates.addAll(importDeclaration.importedDataDeclarations)
                candidates.addAll(importDeclaration.importedNewTypeDeclarations)
                candidates.addAll(importDeclaration.importedTypeSynonymDeclarations)
            }
            return candidates
        }
}
