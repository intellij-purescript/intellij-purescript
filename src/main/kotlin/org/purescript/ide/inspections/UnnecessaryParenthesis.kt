package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.psi.PsiElement
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.declaration.value.binder.ParensBinder
import org.purescript.module.declaration.value.binder.record.RecordLabelExprBinder
import org.purescript.module.declaration.value.expression.PSParens
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.module.declaration.value.expression.RecordAccess
import org.purescript.module.declaration.value.expression.RecordUpdate
import org.purescript.module.declaration.value.expression.controll.caseof.CaseAlternative
import org.purescript.module.declaration.value.expression.controll.ifthenelse.PSIfThenElse
import org.purescript.module.declaration.value.expression.identifier.Argument
import org.purescript.module.declaration.value.expression.identifier.Call
import org.purescript.module.declaration.value.expression.identifier.ExpressionWildcard
import org.purescript.module.declaration.value.expression.namespace.PSLambda
import org.purescript.module.declaration.value.parameters.Parameter

class UnnecessaryParenthesis() : LocalInspectionTool() {
    private val argument = psiElement(Argument::class.java)
    private val twoChildren = collection<PsiElement?>().size(2)
    private val value = psiElement(PSValue::class.java)
    private val lambda = psiElement(PSLambda::class.java)
    private val call = psiElement(Call::class.java)
    private val ifThanElse = psiElement(PSIfThenElse::class.java)
    private val oneChild = collection<PsiElement?>().size(1)
    private val valueWithOneChild = value.withChildren(oneChild)
    private val parentIsArgument = psiElement().withParent(Argument::class.java)
    private val parenthesis: Capture<PSParens> = psiElement(PSParens::class.java)
    private val type: Capture<PSType> = psiElement(PSType::class.java)
    private val hasOnlyOneChild = psiElement().withChildren(oneChild)
    private val parenthesisAroundIfThanElse = parenthesis.withChild(value.withChild(call.withChild(ifThanElse)))
    private val parenthesisAroundLambda = parenthesis.withChild(value.withChild(call.withChild(lambda)))
    val wildcard = psiElement(ExpressionWildcard::class.java)
    val operatorSection = parenthesis.withChild(value.withChild(call.withChild(wildcard)))
    val isTyped = parenthesis.withSuperParent(2, value.withChild(type))
    val recordAccsess = psiElement(RecordAccess::class.java)
    private val lonelyAccessor = parenthesis.withChild(
        value
            .withChildren(oneChild)
            .withChild(call.withChild(recordAccsess))
    )
    private val recordUpdate = psiElement(RecordUpdate::class.java)
    private val recordUpdateArgument = parenthesis.withParent(call.withParent(call.withChild(argument.withChild(recordUpdate))))
    private val pattern = or(
        lonelyAccessor,
        parenthesis
            .withParent(hasOnlyOneChild)
            .withChild(valueWithOneChild)
            .andNot(isTyped)
            .andNot(parentIsArgument)
            .andNot(
                parenthesisAroundIfThanElse
                    .andNot(psiElement().withSuperParent(2, valueWithOneChild))
            )
            .andNot(
                parenthesisAroundLambda
                    .andNot(psiElement().withSuperParent(2, valueWithOneChild))
            )
            .andNot(recordUpdateArgument)
            .andNot(operatorSection),
        parenthesis
            .withParent(hasOnlyOneChild)
            .withSuperParent(2, valueWithOneChild)
            .andNot(operatorSection)
    )
    private val caseAlternative = psiElement(CaseAlternative::class.java)
    private val recordLabelExprBinder = psiElement(RecordLabelExprBinder::class.java)

    private val hasOnlyTwoChildren = psiElement()
        .withChildren(twoChildren)

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

