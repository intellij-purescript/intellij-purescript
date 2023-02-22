package org.purescript.psi.declaration.value

import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.usages.PsiNamedElementUsageGroupBase
import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageTarget
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.UsageGroupingRule
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.module.Module

class ValueParentUsageGroupingRule : UsageGroupingRule {
    override fun getParentGroupsFor(usage: Usage, targets: Array<out UsageTarget>): MutableList<UsageGroup> {
        val element = (usage as? PsiElementUsage)?.element ?: return mutableListOf()
        return when (element) {
            is ExpressionAtom -> {
                val values = element
                    .parentsOfType<ValueDeclarationGroup>(true)
                    .map { PsiNamedElementUsageGroupBase(it) }
                    .toList()
                    .reversed()
                
                element
                    .parentOfType<Module>()
                    ?.let { listOf(PsiNamedElementUsageGroupBase(it)) + values }
                    ?.toMutableList()
                    ?: values.toMutableList()
            }

            else -> mutableListOf()
        }
    }
}