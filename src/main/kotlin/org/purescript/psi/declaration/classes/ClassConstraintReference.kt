package org.purescript.psi.declaration.classes

import com.intellij.psi.PsiReferenceBase

class ClassConstraintReference(classConstraint: PSClassConstraint) : PsiReferenceBase<PSClassConstraint>(
    classConstraint,
    classConstraint.identifier.textRangeInParent,
    false
) {

    override fun getVariants(): Array<PSClassDeclaration> =
        candidates.toTypedArray()

    override fun resolve(): PSClassDeclaration? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<PSClassDeclaration>
        get() = myElement.module?.run {
            cache.classes.toList() + cache.imports.flatMap { it.importedClassDeclarations }
        } ?: emptyList()
}
