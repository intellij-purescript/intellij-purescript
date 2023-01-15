package org.purescript.psi.declaration.classes

import com.intellij.psi.PsiReferenceBase

class ClassConstraintReference(classConstraint: PSClassConstraint) : PsiReferenceBase<PSClassConstraint>(
    classConstraint,
    classConstraint.identifier.textRangeInParent,
    false
) {

    override fun getVariants(): Array<ClassDecl> =
        candidates.toTypedArray()

    override fun resolve(): ClassDecl? =
        candidates.firstOrNull { it.name == myElement.name }

    private val candidates: List<ClassDecl>
        get() = myElement.module?.run {
            cache.classes.toList() + cache.imports.flatMap { it.importedClassDeclarations }
        } ?: emptyList()
}
