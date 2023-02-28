package org.purescript.psi.declaration.imports

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.psi.declaration.data.DataDeclaration
import org.purescript.psi.declaration.newtype.NewtypeDecl

class ImportedDataMemberReference(element: PSImportedDataMember) :
    PsiReferenceBase<PSImportedDataMember>(element, element.properName.textRangeInParent, false) {
    override fun getVariants(): Array<Any> = candidates.toTypedArray()
    override fun resolve(): PsiElement? = candidates.firstOrNull { it.name == element.name }
    private val candidates: List<PsiNamedElement>
        get() = when (val declaration = element.dataDeclarationImport?.reference?.resolve()) {
            is NewtypeDecl -> listOf(declaration.newTypeConstructor)
            is DataDeclaration.Psi -> declaration.dataConstructors.asList()
            else -> listOf<PsiNamedElement>()
        }
}
