package org.purescript.ide.refactoring

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.childrenOfType
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.alsoIfNull
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.value.ValueDecl
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.exports.RecordLabel
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.expression.PSExpressionWhere
import org.purescript.psi.expression.PSLet
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
            val call = usage.element as? PSExpressionIdentifier ?: continue
            val copy = valueDeclaration.copy() as ValueDecl
            val copyBinders = copy.parameters?.binderAtoms?.filterIsInstance<PSVarBinder>()
                ?: emptyList()
            val replace = copyBinders.map {
                ReferencesSearch
                    .search(it, LocalSearchScope(copy))
                    .findAll()
                    .map { it.element }
            }.toList()
            val arguments = call.arguments.toList()
            if (replace.size > arguments.size) error("There mus be more arguments then parameters")
            val argumentsToInline = arguments.take(replace.size)
            replace.zip(argumentsToInline) { refs, b ->
                refs.forEach { it.replace(b) }
            }
            val expression = copy.value.text
            val parenthesis = factory.createParenthesis(expression) ?: return
            if (argumentsToInline.isNotEmpty()) {
                call.parent.deleteChildRange(call.nextSibling, argumentsToInline.last())
            }
            when (usage.element?.parent) {
                is RecordLabel -> factory
                    .createRecordLabel("${(usage.element as PSExpressionIdentifier).name}: $expression")
                    ?.let { usage.element?.parent?.replace(it) }
                    ?: usage.element?.replace(parenthesis)

                else -> usage.element?.replace(parenthesis)
            }
        }
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
