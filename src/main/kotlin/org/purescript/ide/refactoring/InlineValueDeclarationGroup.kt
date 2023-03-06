package org.purescript.ide.refactoring

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.alsoIfNull
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.exports.RecordLabel
import org.purescript.psi.expression.*
import org.purescript.psi.expression.dostmt.PSDoNotationLet

class InlineValueDeclarationGroup(val project: Project, val toInline: ValueDeclarationGroup) :
    BaseRefactoringProcessor(project) {
    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>): UsageViewDescriptor =
        BaseUsageViewDescriptor(toInline)

    override fun findUsages(): Array<UsageInfo> =
        ReferencesSearch
            .search(toInline, GlobalSearchScope.projectScope(project))
            .findAll()
            .map(::UsageInfo)
            .toTypedArray()

    override fun performRefactoring(usages: Array<out UsageInfo>) {
        val valueDeclaration = toInline.valueDeclarations.singleOrNull()
            ?: error("can only inline value declarations with one body")
        val binders = valueDeclaration.parameters?.binderAtoms ?: emptyList()
        if (binders.any { it !is PSVarBinder }) error("can only inline simple parameters")
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
        when (val parent = toInline.parent) {
            is PSLet ->
                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                    parent.value?.let { parent.parent.parent.replace(it) }
                        .alsoIfNull { toInline.delete() }
                } else {
                    toInline.delete()
                }

            is PSDoNotationLet, is PSExpressionWhere ->
                if (parent.childrenOfType<ValueDeclarationGroup>()?.size == 1) {
                    parent.delete()
                } else {
                    toInline.delete()
                }

            else -> toInline.delete()
        }
    }

    override fun getCommandName(): String = "Inline function ${toInline.name}"

}
