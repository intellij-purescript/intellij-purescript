package org.purescript.ide.purs

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ex.ExternalAnnotatorBatchInspection

class PursIdeRebuildInspection: LocalInspectionTool(), ExternalAnnotatorBatchInspection {

    override fun getShortName(): String {
        return SHORT_NAME
    }

    companion object {
        val SHORT_NAME = "Rebuild"
    }
}