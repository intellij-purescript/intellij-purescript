package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import org.purescript.psi.binder.Parameter
import org.purescript.psi.binder.ParensBinder
import org.purescript.psi.expression.Argument
import org.purescript.psi.expression.PSParens
import org.purescript.psi.expression.PSValue
import org.purescript.psi.expression.caseof.PSCaseAlternative

class UnnecessaryParenthesis : LocalInspectionTool() {
    private val valueWithOneChild = psiElement(PSValue::class.java)
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(1))
    private val parentIsArgument = psiElement().withParent(Argument::class.java)
    private val parenthesis: Capture<PSParens> = psiElement(PSParens::class.java)
    private val parenthesisBinder: Capture<ParensBinder> = psiElement(ParensBinder::class.java)
    private val hasOnlyOneChild = psiElement()
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(1))
    private val pattern = parenthesis
        .andOr(
            psiElement()
                .withChild(valueWithOneChild)
                .andNot(parentIsArgument),
            psiElement().withSuperParent(2, valueWithOneChild)
        )


    private val caseAlternative = psiElement(PSCaseAlternative::class.java)

    private val binder = psiElement().andOr(
        psiElement().withParent(
            hasOnlyOneChild
                .andNot(psiElement(Parameter::class.java))
        ),
        psiElement().withParent(caseAlternative),
    )

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        visitElement {
            when (this) {
                is PSParens ->
                    if (pattern.accepts(this)) {
                        holder.registerProblem(
                            this,
                            "Unnecessary parentheses",
                            RemoveParenthesis(this),
                        )
                    }

                is ParensBinder -> if (binder.accepts(this)) {
                    holder.registerProblem(
                        this,
                        "Unnecessary parentheses",
                        RemoveParenthesisBinder(this),
                    )
                }
            }

        }
}

