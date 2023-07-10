package org.purescript

import com.intellij.openapi.components.Service
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedData
import org.purescript.ide.formatting.ImportedOperator
import org.purescript.ide.formatting.ImportedValue

@Service
class PackageSet {
    val reverseLookup: MutableMap<String, MutableList<Pair<String, ImportDeclaration>>> = mutableMapOf()

    init {
        val latest = this::class.java.classLoader.getResource("package-set/latest.json").readText()
        val packageList = Json.decodeFromString<Map<String, Map<String, List<Map<String, String>>>>>(latest)
        for ((packageName, packageObject) in packageList) {
            for ((moduleName, exports) in packageObject) {
                for (export in exports) {
                    val importType : String = export["import type"] ?: continue
                    val key = when (importType) {
                            "value", "operator" -> export["name"] ?: continue
                            "data member" -> export["constructor"] ?: continue
                            else -> continue
                    }
                    val importDeclaration = ImportDeclaration(moduleName)
                    val payload = when (importType) {
                        "value" -> importDeclaration.withItems(ImportedValue(key))
                        "operator" -> importDeclaration.withItems(ImportedOperator(key))
                        "data member" -> {
                            val type = export["type"] ?: continue
                            importDeclaration.withItems(ImportedData(type, dataMembers = setOf(key)))
                        }
                        else -> continue
                    }
                    if (key in reverseLookup) {
                        reverseLookup[key]?.add(packageName to payload)
                    } else {
                        reverseLookup[key] = mutableListOf(packageName to payload)
                    }
                }
            }
        }
    }
}