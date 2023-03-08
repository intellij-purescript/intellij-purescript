package org.purescript.run.purs

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection

class PursIdeRebuildInspection: LocalInspectionTool(), ExternalAnnotatorBatchInspection {

    override fun getShortName(): String = SHORT_NAME

    companion object {
        const val SHORT_NAME = "Rebuild"
    }
}