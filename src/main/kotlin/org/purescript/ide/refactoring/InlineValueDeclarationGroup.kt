package org.purescript.ide.refactoring

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.childrenOfType
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.alsoIfNull
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.expression.PSExpressionWhere
import org.purescript.psi.expression.PSLet
import org.purescript.psi.expression.dostmt.PSDoNotationLet

class InlineValueDeclarationGroup(
    val project: Project,
    val toInline: ValueDeclarationGroup
) : BaseRefactoringProcessor(project) {
    override fun createUsageViewDescriptor(usages: Array<out UsageInfo>): UsageViewDescriptor {
        return BaseUsageViewDescriptor(toInline)
    }

    override fun findUsages(): Array<UsageInfo> =
        ReferencesSearch
            .search(toInline, GlobalSearchScope.projectScope(project))
            .findAll()
            .map(::UsageInfo)
            .toTypedArray()

    override fun performRefactoring(usages: Array<out UsageInfo>) {
        val expression = toInline.valueDeclarations.single().value.text
        val factory = project.service<PSPsiFactory>()
        val parenthesis = factory.createParenthesis(expression) ?: return
        for (usage in usages) usage.element?.replace(parenthesis)
        when (val parent = toInline.parent) {
            is PSLet ->
                if (parent.childrenOfType<ValueDeclarationGroup>()?.size == 1) {
                    parent.value?.let { parent.parent.replace(it) }
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
