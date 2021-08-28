package org.purescript.ide.refactoring

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSTypeImpl
import org.purescript.psi.declaration.PSSignature
import org.purescript.psi.declaration.PSValueDeclaration

class PSInlinePSValueDeclaration(val project: Project, val toInline: PSValueDeclaration) : BaseRefactoringProcessor(project) {
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
        val value = toInline.value ?: return
        usages.asIterable().forEach loop@{
            val copy = value.copy()
            val element = it.reference?.element ?: return@loop
            if (element is PSSignature) {
                element.delete()
                return@loop
            }
            val parent = element.parent
            if (parent.children.size == 1) {
                parent.addRangeAfter(copy.firstChild, copy.lastChild, element)
            } else {
                val parenthesis = PSPsiFactory(project).createParenthesis(copy.text) ?: return@loop
                element.replace(parenthesis)
            }

            element.delete()
        }
        toInline.delete()
    }

    override fun getCommandName(): String = "Inline function ${toInline.name}"

}
