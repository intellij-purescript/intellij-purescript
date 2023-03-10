package org.purescript.module.declaration.fixity

import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase

class ConstructorFixityReference(fixity: FixityDeclaration) :
    PsiReferenceBase<FixityDeclaration>(
        fixity,
        fixity.qualifiedProperName?.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? {
        val name = element.qualifiedProperName?.properName?.name
        return candidates.firstOrNull { it.name == name }
    }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = element.qualifiedProperName?.moduleName?.name
            return sequence {
                if (qualifyingName == null) {
                    yieldAll(module.cache.dataConstructors.toList())
                }
                val importDeclarations =
                    module.cache.imports.filter { it.importAlias?.name == qualifyingName }
                yieldAll(importDeclarations.flatMap { it.importedDataConstructors })
            }
        }
}