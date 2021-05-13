package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.siblings
import org.purescript.PSLanguage
import org.purescript.psi.binder.PSConstructorBinderImpl
import org.purescript.psi.exports.PSExportedModule
import org.purescript.psi.exports.PSExportedOperator
import org.purescript.psi.exports.PSExportedValue
import org.purescript.psi.expression.PSExpressionConstructor
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.expression.PSExpressionOperator
import org.purescript.psi.expression.PSExpressionSymbol
import org.purescript.psi.imports.PSImportDeclarationImpl
import org.purescript.psi.imports.PSImportedOperator
import org.purescript.psi.typeconstructor.PSTypeConstructor

class PSUnresolvedReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                when (element) {
                    is PSExportedValue -> visitReference(element.reference)
                    is PSExportedOperator -> visitReference(element.reference)
                    is PSExportedModule -> visitModuleReference(element.reference)
                    is PSImportDeclarationImpl -> visitModuleReference(element.reference)
                    is PSImportedOperator -> visitReference(element.reference)
                    is PSExpressionConstructor -> visitReference(element.reference)
                    is PSConstructorBinderImpl -> visitReference(element.reference)
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
                if (reference.canonicalText in PSLanguage.BUILTIN_TYPES) {
                    return
                }

                // TODO Workaround to prevent false positives on class constraints
                val isClassConstraint = reference.element.parent
                    .siblings(forward = true, withSelf = false)
                    .any { it.text == "=>" }
                if (isClassConstraint) {
                    return
                }

                // TODO Workaround to prevent false positives on qualified types
                if (reference.element.textContains('.')) {
                    return
                }

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
