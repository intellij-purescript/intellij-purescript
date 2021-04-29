package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import org.purescript.PSLanguage
import org.purescript.psi.PSConstructorBinderImpl
import org.purescript.psi.exports.PSExportedModule
import org.purescript.psi.exports.PSExportedValue
import org.purescript.psi.expression.PSExpressionConstructor
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.expression.PSExpressionSymbol
import org.purescript.psi.imports.PSImportDeclarationImpl

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
                    is PSExportedModule -> visitModuleReference(element.reference)
                    is PSImportDeclarationImpl -> visitModuleReference(element.reference)
                    is PSExpressionConstructor -> visitReference(element.reference)
                    is PSConstructorBinderImpl -> visitReference(element.reference)
                    is PSExpressionIdentifier -> visitReference(element.reference)
                    is PSExpressionSymbol -> visitReference(element.reference)
                }
            }

            private fun visitModuleReference(reference: PsiReference) {
                if (reference.canonicalText !in PSLanguage.BUILTIN_MODULES) {
                    visitReference(reference)
                }
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
