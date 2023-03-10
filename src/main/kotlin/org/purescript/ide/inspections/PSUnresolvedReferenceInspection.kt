package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import org.purescript.PSLanguage
import org.purescript.module.declaration.value.binder.ConstructorBinder
import org.purescript.module.exports.ExportedModule
import org.purescript.module.exports.ExportedOperator
import org.purescript.module.exports.ExportedValue
import org.purescript.module.declaration.value.expression.identifier.PSExpressionConstructor
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.module.declaration.value.expression.identifier.PSExpressionOperator
import org.purescript.module.declaration.value.expression.identifier.PSExpressionSymbol
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.imports.PSImportedOperator
import org.purescript.module.declaration.type.typeconstructor.PSTypeConstructor

class PSUnresolvedReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                when (element) {
                    is ExportedValue.Psi -> visitReference(element.reference)
                    is ExportedOperator.Psi -> visitReference(element.reference)
                    is ExportedModule -> visitModuleReference(element.reference)
                    is Import -> visitModuleReference(element.reference)
                    is PSImportedOperator -> visitReference(element.reference)
                    is PSExpressionConstructor -> visitReference(element.reference)
                    is ConstructorBinder -> visitReference(element.reference)
                    is PSExpressionIdentifier -> visitReference(element.reference)
                    is PSExpressionSymbol -> visitReference(element.reference)
                    is PSExpressionOperator -> visitReference(element.reference)
                    is PSTypeConstructor -> visitTypeReference(element.reference)
                }
            }

            private fun visitModuleReference(reference: PsiReference) {
                if (reference.canonicalText !in PSLanguage.BUILTIN_MODULES) {
                    visitReference(reference)
                }
            }

            private fun visitTypeReference(reference: PsiReference) {
                if (reference.canonicalText in PSLanguage.BUILTIN_TYPES) return
                visitReference(reference)
            }

            private fun visitReference(reference: PsiReference) {
                when(reference) {
                    is PsiReferenceBase.Poly<*> -> {
                        if (reference.multiResolve(false).isEmpty()) {
                            holder.registerProblem(reference)
                        }
                    }
                    else -> {
                        if (reference.resolve() == null) {
                            holder.registerProblem(reference)
                        }
                    }
                }
            }
        }
    }
}
