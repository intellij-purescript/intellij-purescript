package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import org.purescript.module.declaration.value.binder.ParensBinder
import org.purescript.module.declaration.value.binder.record.RecordLabelExprBinder
import org.purescript.module.declaration.value.expression.*
import org.purescript.module.declaration.value.expression.controll.caseof.CaseAlternative
import org.purescript.module.declaration.value.expression.identifier.Argument
import org.purescript.module.declaration.value.expression.identifier.Call
import org.purescript.module.declaration.value.expression.identifier.ExpressionWildcard
import org.purescript.module.declaration.value.parameters.Parameter

class UnnecessaryParenthesis() : LocalInspectionTool() {
    private val argument = psiElement(Argument::class.java)
    private val twoChildren = collection<PsiElement?>().size(2)
    private val call = psiElement(Call::class.java)
    private val oneChild = collection<PsiElement?>().size(1)
    private val parenthesis: Capture<PSParens> = psiElement(PSParens::class.java)
    private val hasOnlyOneChild = psiElement().withChildren(oneChild)
    val wildcard = psiElement(ExpressionWildcard::class.java)
    val recordAccsess = psiElement(RecordAccess::class.java)
    private val recordUpdate = psiElement(RecordUpdate::class.java)
    private val value = psiElement(PSValue::class.java)

    private val neccesery = or(
        parenthesis.withChild(value.withChild(wildcard)),
        parenthesis.withParent(argument).withChild(or(call, value)),
        parenthesis.withParent(
            or(
                recordAccsess,
                psiElement(TypedExpression::class.java),
                call.withChild(argument.withChild(recordUpdate)),
                value
            )
        ),
        parenthesis.withParent(call).withChild(value)
    )

    private val pattern = and(parenthesis, not(neccesery))
    private val caseAlternative = psiElement(CaseAlternative::class.java)
    private val recordLabelExprBinder = psiElement(RecordLabelExprBinder::class.java)

    private val hasOnlyTwoChildren = psiElement().withChildren(twoChildren)

    private val binder = or(
        psiElement().withParent(hasOnlyOneChild.andNot(psiElement(Parameter::class.java))),
        psiElement().withParent(caseAlternative.and(hasOnlyTwoChildren)),
        psiElement().withParent(recordLabelExprBinder.and(hasOnlyOneChild)),
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

