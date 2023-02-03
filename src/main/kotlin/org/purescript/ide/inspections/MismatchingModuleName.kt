package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import org.purescript.file.PSFile
import java.nio.file.Paths

class MismatchingModuleName : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        visitFile {
            val module = (this as? PSFile)?.module ?: return@visitFile
            val moduleName = suggestModuleName()  ?: return@visitFile
            val oldModuleName = module.name
            if (moduleName != oldModuleName) {
                val description =
                    "According to module file location the module name should be '$moduleName'"
                holder.registerProblem(
                    module.nameIdentifier,
                    description,
                    ChangeModuleName(module, moduleName),
                    MoveModuleMatchingName(module)
                )
            }
        }
}

