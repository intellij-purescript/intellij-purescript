package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import org.purescript.psi.exports.PSExportedModule
import org.purescript.psi.exports.PSExportedValue
import org.purescript.psi.imports.PSImportDeclarationImpl

class PSUnresolvedReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                when (element) {
                    is PSExportedValue -> visitReference(element.reference)
                    is PSExportedModule -> visitReference(element.reference)
                    is PSImportDeclarationImpl -> visitReference(element.reference)
                }
            }

            private fun visitReference(reference: PsiReference) {
                if (reference.resolve() == null) {
                    holder.registerProblem(reference)
                }
            }
        }
    }
}
