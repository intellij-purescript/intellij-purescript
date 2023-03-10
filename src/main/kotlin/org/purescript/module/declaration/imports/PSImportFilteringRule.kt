package org.purescript.module.declaration.imports

import com.intellij.psi.util.parentsOfType
import com.intellij.usages.Usage
import com.intellij.usages.UsageTarget
import com.intellij.usages.rules.ImportFilteringRule
import com.intellij.usages.rules.PsiElementUsage
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSStubbedElement

class PSImportFilteringRule : ImportFilteringRule() {
    override fun isVisible(usage: Usage, targets: Array<out UsageTarget>): Boolean {
        val element = (usage as? PsiElementUsage)?.element ?: return true
        return if (element is PSPsiElement || element is PSStubbedElement<*>) {
            !element.parentsOfType<Import>(true).any { true }
        } else {
            true
        }
    }
}