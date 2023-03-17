package org.purescript.ide.refactoring

import com.intellij.openapi.components.service
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.alsoIfNull
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.dostmt.PSDoNotationLet
import org.purescript.module.declaration.value.expression.identifier.Argument
import org.purescript.module.declaration.value.expression.identifier.Call
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.module.declaration.value.expression.literals.RecordLabel
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.module.declaration.value.expression.namespace.PSLet
import org.purescript.psi.PSPsiFactory

class InlineValueDeclarationGroup(private val dialog: InlineDialog<ValueDeclarationGroup, PSExpressionIdentifier>) :
    BaseRefactoringProcessor(dialog.project) {
    private val toInline get() = dialog.toInline
    private val location get() = dialog.location
    private val project get() = dialog.project
    private val isInlineThisOnly get() = dialog.isInlineThisOnly

    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>): UsageViewDescriptor =
        BaseUsageViewDescriptor(dialog.toInline)

    override fun findUsages(): Array<UsageInfo> =
        if (isInlineThisOnly) when (val ref = location?.reference) {
            null -> arrayOf() // TODO: show hint for user
            else -> arrayOf(UsageInfo(ref))
        }
        else toInline.findUsages().toTypedArray()

    override fun performRefactoring(usages: Array<out UsageInfo>) {
        val valueDeclaration = toInline.valueDeclarations.singleOrNull()
            ?: error("can only inline value declarations with one body")
        val binders = valueDeclaration.parameterList?.parameterBinders ?: emptyList()
        if (binders.any { it !is VarBinder }) error("can only inline simple parameters")
        val factory = project.service<PSPsiFactory>()
        for (usage in usages) {
            val element = usage.element as? PSExpressionIdentifier ?: continue
            val arguments = element.arguments.toList()
            when (val parent = element.parent) {
                is Call -> element
                    .parentsOfType<Call>()
                    .drop(arguments.size)
                    .first()
                    .replace(valueDeclaration
                        .inline(arguments)
                        .let { it.withParenthesis()?.parent ?: it }
                    )

                is RecordLabel -> factory
                    .createRecordLabel("${element.name}: ${valueDeclaration.inline(arguments).text}")
                    ?.let { parent.replace(it) }
                    ?: element.replace(valueDeclaration.inline(arguments))

                is Argument -> element.replace(
                    element.replace(valueDeclaration
                        .inline(arguments)
                        .let { it.withParenthesis() ?: it })
                )

                else -> element.replace(
                    element.replace(valueDeclaration
                        .inline(arguments)
                        .let { it.withParenthesis() ?: it })
                )
            }
        }
        // delete declaration
        if (!isInlineThisOnly) when (val parent = toInline.parent) {
            is PSLet ->
                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                    parent.value?.let { parent.parent.parent.replace(it) }
                        .alsoIfNull { toInline.delete() }
                } else {
                    toInline.delete()
                }

            is PSDoNotationLet, is PSExpressionWhere ->
                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                    parent.delete()
                } else {
                    toInline.delete()
                }

            else -> toInline.delete()
        }
    }

    override fun getCommandName(): String = "Inline function ${toInline.name}"

}
