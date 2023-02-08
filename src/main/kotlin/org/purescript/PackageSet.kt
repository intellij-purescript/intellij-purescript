package org.purescript

import com.google.gson.Gson
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.purescript.run.purs.PursIdeAddClauseQuickFix

@Service
class PackageSet(project: Project) {
    val reverseLookup: MutableMap<String, MutableList<Pair<String, String>>> = mutableMapOf()
    init {
        val latest = this::class.java.classLoader.getResource("package-set/latest.json").readText()
        val packageList = Json.decodeFromString<Map<String,Map<String, List<String>>>>(latest)
        for ((packageName, packageObject) in packageList) {
            for ((moduleName, exports) in packageObject) {
                for (export in exports) {
                    if (export in reverseLookup) {
                        reverseLookup[export]?.add(packageName to moduleName)
                    } else {
                        reverseLookup.set(export, mutableListOf(packageName to moduleName))
                    }
                }
            }
        }
    }
}