package org.purescript.module.declaration.imports

import com.intellij.psi.PsiReferenceBase
import org.purescript.module.declaration.classes.ClassDecl

class ImportedClassReference(importedClass: PSImportedClass) : PsiReferenceBase<PSImportedClass>(
    importedClass,
    importedClass.properName.textRangeInParent,
    false
) {

    override fun getVariants(): Array<Any> =
        candidates.toTypedArray()

    override fun resolve(): ClassDecl? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<ClassDecl>
        get() = myElement.importDeclaration.importedModule?.exportedClassDeclarations
            ?: emptyList()
}
