package org.purescript.psi.declaration.value

import com.intellij.openapi.project.Project
import com.intellij.usages.UsageViewPresentation
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.rules.UsageGroupingRule
import com.intellij.usages.rules.UsageGroupingRuleProvider

class ValueUsageGroupingRuleProvider: UsageGroupingRuleProvider {
    override fun getActiveRules(
        project: Project,
        usageViewSettings: UsageViewSettings,
        presentation: UsageViewPresentation?
    ): Array<UsageGroupingRule> {
        if (usageViewSettings.isGroupByFileStructure) {
            return arrayOf(ValueParentUsageGroupingRule())
        } else {
            return emptyArray()
        }
    }
}