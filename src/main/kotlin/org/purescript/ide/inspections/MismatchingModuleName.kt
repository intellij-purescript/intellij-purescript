package org.purescript.ide.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import org.purescript.file.PSFile
import java.nio.file.Paths

class MismatchingModuleName : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean) =
        visitFile {
            val module = (this as? PSFile)?.module ?: return@visitFile
            val oldModuleName = module.name
            val fileName = name.removeSuffix(".purs")
            val directoryPath = Paths.get(parent?.virtualFile?.path ?: return@visitFile)
            val relativePath = try {
                project
                    .basePath
                    ?.let { Paths.get(it).toAbsolutePath() }
                    ?.relativize(directoryPath.toAbsolutePath())
                    ?: directoryPath
            } catch (ignore: IllegalArgumentException) {
                directoryPath
            }
            val moduleName = relativePath
                .reversed()
                .takeWhile { "$it" != "src" && "$it" != "test" }
                .filter { "$it".first().isUpperCase() }
                .reversed()
                .joinToString(".")
                .let {
                    "$it.$fileName"
                }
                .removePrefix(".")
            if (moduleName != oldModuleName) {
                val description =
                    "According to module file location the module name should be '$moduleName'"
                holder.registerProblem(
                    module.nameIdentifier,
                    description,
                    ChangeModuleName(module, moduleName)
                )
            }
        }
}

