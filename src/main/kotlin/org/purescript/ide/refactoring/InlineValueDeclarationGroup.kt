package org.purescript.ide.refactoring

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.declaration.value.ValueDeclarationGroup

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
        toInline.delete()
    }

    override fun getCommandName(): String = "Inline function ${toInline.name}"

}
