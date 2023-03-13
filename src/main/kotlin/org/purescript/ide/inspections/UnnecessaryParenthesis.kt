package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import org.purescript.module.declaration.type.PSType
import org.purescript.module.declaration.value.binder.ParensBinder
import org.purescript.module.declaration.value.binder.record.RecordLabelExprBinder
import org.purescript.module.declaration.value.expression.PSParens
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.module.declaration.value.expression.controll.caseof.CaseAlternative
import org.purescript.module.declaration.value.expression.controll.ifthenelse.PSIfThenElse
import org.purescript.module.declaration.value.expression.identifier.Argument
import org.purescript.module.declaration.value.expression.identifier.Call
import org.purescript.module.declaration.value.expression.identifier.ExpressionWildcard
import org.purescript.module.declaration.value.parameters.Parameter

class UnnecessaryParenthesis : LocalInspectionTool() {
    private val value = psiElement(PSValue::class.java)
    private val call = psiElement(Call::class.java)
    private val ifThanElse = psiElement(PSIfThenElse::class.java)

    private val valueWithOneChild = value
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(1))
    private val parentIsArgument = psiElement().withParent(Argument::class.java)
    private val parenthesis: Capture<PSParens> = psiElement(PSParens::class.java)
    private val type: Capture<PSType> = psiElement(PSType::class.java)
    private val hasOnlyOneChild = psiElement()
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(1))
    private val parenthesisAroundIfThanElse =
        parenthesis.withChild(value.withChild(call.withChild(ifThanElse)))
    val wildcard = psiElement(ExpressionWildcard::class.java)
    val operatorSection = parenthesis.withChild(value.withChild(call.withChild(wildcard)))
    val isTyped = parenthesis.withSuperParent(2, value.withChild(type))
    private val pattern = or(
        parenthesis
            .withParent(hasOnlyOneChild)
            .withChild(valueWithOneChild)
            .andNot(isTyped)
            .andNot(parentIsArgument)
            .andNot(
                parenthesisAroundIfThanElse
                    .andNot(psiElement().withSuperParent(2, valueWithOneChild))
            )
            .andNot(operatorSection)
        ,
        parenthesis
            .withParent(hasOnlyOneChild)
            .withSuperParent(2, valueWithOneChild)
            .andNot(operatorSection)
    )
    private val caseAlternative = psiElement(CaseAlternative::class.java)
    private val recordLabelExprBinder = psiElement(RecordLabelExprBinder::class.java)

    private val hasOnlyTwoChildren = psiElement()
        .withChildren(PlatformPatterns.collection<PsiElement?>().size(2))

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

