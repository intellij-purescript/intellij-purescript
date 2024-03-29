package org.purescript

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

class PurescriptLiveTemplateContext : TemplateContextType("Purescript") {
    override fun isInContext(templateActionContext: TemplateActionContext) =
        templateActionContext.file.name.endsWith(".purs")
}