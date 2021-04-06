package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.SmartPointerManager

class ExpressionConstructorReference(expressionConstructor: PSExpressionConstructor) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSExpressionConstructor>(
        expressionConstructor,
        expressionConstructor.qualifiedProperName.properName.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? =
        candidates.firstOrNull { it.name == element.name }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            return sequence {
                for (newTypeDeclaration in module.newTypeDeclarations) {
                    yield(newTypeDeclaration.newTypeConstructor)
                }
                for (dataDeclaration in module.dataDeclarations) {
                    yieldAll(dataDeclaration.dataConstructors.iterator())
                }
                for (importDeclaration in module.importDeclarations) {
                    for (importedNewTypeDeclaration in importDeclaration.importedNewTypeDeclarations) {
                        yield(importedNewTypeDeclaration.newTypeConstructor)
                    }
                    for (importedDataDeclaration in importDeclaration.importedDataDeclarations) {
                        yieldAll(importedDataDeclaration.dataConstructors.iterator())
                    }
                }
            }
        }

    override fun getQuickFixes(): Array<LocalQuickFix> =
        arrayOf(ImportExpressionConstructorQuickFix(SmartPointerManager.createPointer(element)))
}
