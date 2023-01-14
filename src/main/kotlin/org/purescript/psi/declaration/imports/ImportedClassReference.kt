package org.purescript.psi.declaration.imports

import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.declaration.classes.PSClassDeclaration

class ImportedClassReference(importedClass: PSImportedClass) : PsiReferenceBase<PSImportedClass>(
    importedClass,
    importedClass.properName.textRangeInParent,
    false
) {

    override fun getVariants(): Array<Any> =
        candidates.toTypedArray()

    override fun resolve(): PSClassDeclaration? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<PSClassDeclaration>
        get() = myElement.importDeclaration?.importedModule?.exportedClassDeclarations
            ?: emptyList()
}
