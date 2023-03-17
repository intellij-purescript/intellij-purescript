package org.purescript.ide.refactoring

import com.intellij.psi.PsiElement
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import org.purescript.psi.UsedElement

class InlineProcessor<ToInline: UsedElement, Location: PsiElement>(
    private val dialog: InlineDialog<ToInline, Location>,
    private val perform: InlineProcessor<ToInline, Location>.(usages: Array<UsageInfo>) -> Unit
) : BaseRefactoringProcessor(dialog.project) {
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

    override fun performRefactoring(usages: Array<UsageInfo>) = this.perform(usages)
    override fun getCommandName(): String = "Inline ${toInline.name}"

}