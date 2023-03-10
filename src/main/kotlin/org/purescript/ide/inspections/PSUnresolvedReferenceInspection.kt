package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import org.purescript.PSLanguage
import org.purescript.psi.binder.ConstructorBinder
import org.purescript.psi.exports.ExportedModule
import org.purescript.psi.exports.ExportedOperator
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.expression.PSExpressionConstructor
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.expression.PSExpressionOperator
import org.purescript.psi.expression.PSExpressionSymbol
import org.purescript.psi.declaration.imports.Import
import org.purescript.psi.declaration.imports.PSImportedOperator
import org.purescript.psi.type.typeconstructor.PSTypeConstructor

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
                    is PSExpressionConstructor -> visitReference(element.getReference())
                    is ConstructorBinder -> visitReference(element.reference)
                    is PSExpressionIdentifier -> visitReference(element.getReference())
                    is PSExpressionSymbol -> visitReference(element.getReference())
                    is PSExpressionOperator -> visitReference(element.getReference())
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
