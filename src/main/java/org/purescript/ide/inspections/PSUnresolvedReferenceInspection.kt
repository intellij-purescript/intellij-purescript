package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.purescript.psi.exports.PSExportedModule
import org.purescript.psi.exports.PSExportedValue

class PSUnresolvedReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                when (element) {
                    is PSExportedValue -> {
                        if (element.reference.multiResolve(false).isEmpty()) {
                            holder.registerProblem(
                                element,
                                "Unresolved reference '${element.name}'",
                                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
                            )
                        }
                    }
                    is PSExportedModule -> {
                        if (element.reference.resolve() == null) {
                            holder.registerProblem(
                                element,
                                "Unresolved module '${element.name}'",
                                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
                            )
                        }
                    }
                }
            }
        }
    }
}
