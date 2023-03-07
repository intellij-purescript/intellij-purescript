package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import org.purescript.psi.expression.Call
import org.purescript.psi.expression.PSParens

class UnnecessaryParenthesis : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        visitElement {
            if (this is PSParens && parent is Call && value?.children?.size == 1) {
                holder.registerProblem(
                    this,
                    "Unnecessary parentheses",
                    RemoveParenthesis(this),
                )
            }
        }
}

