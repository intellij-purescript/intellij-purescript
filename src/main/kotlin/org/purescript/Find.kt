package org.purescript

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.stubs.StubIndexKey.createIndexKey
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.value.ValueDeclarationGroup

typealias ValueKey = StubIndexKey<String?, ValueDeclarationGroup>
typealias ImportKey = StubIndexKey<String?, Import>
class Find(val project: Project) {

    fun exportedTopLevelValuesInModule(moduleName: String) = values(exportedValuesByModuleName, moduleName)
    fun topLevelValuesInModule(moduleName: String) = values(topLevelValuesByModule, moduleName)
    fun exportedImportsInModule(moduleName: String) = imports(exportedImportsInModule, moduleName)

    fun importsInModule(module:String, alias:String? = null) = when {
        alias != null -> imports(importsInModuleAndWithAlias, "$module&$alias")
        else -> imports(importsInModuleWithoutAlias, module)
    }

    fun imports(key: ImportKey, moduleName: String, scope: GlobalSearchScope? = null): Collection<Import> =
        StubIndex.getElements(key, moduleName, project, scope, Import::class.java)
    fun values(key: ValueKey, value: String, scope: GlobalSearchScope? = null): Collection<ValueDeclarationGroup> =
        StubIndex.getElements(key, value, project, scope, ValueDeclarationGroup::class.java)

    fun importedValueInModule(value:String, module: String, alias:String?=null) =
        Query(this, value,).find(module, alias)

    fun valueImportedFromModule(value:String, module: String) =
        Query(this, value,).importAll(module)

    class Query(val find: Find, val valueName: String) {
        val visited = mutableSetOf<String>()
        fun find(at: String, alias: String? = null): ValueDeclarationGroup? {
            if (at in visited) return null
            visited.add(at)

            val imports = find.importsInModule(at, alias)
            for (import in imports) {
                val res = valueFromImport(import)
                if (res != null) return res
            }
            return null
        }

        private fun valueFromImport(import: Import): ValueDeclarationGroup? {
            val importItems = import.importList
            return when {
                importItems == null -> importAll(import.moduleNameName)
                importItems.isHiding -> importAllBut(import.moduleNameName, importItems.values)
                else -> importOnly(import.moduleNameName, importItems.values)
            }
        }

        private fun importOnly(importedModule: String, values: Set<String>): ValueDeclarationGroup? {
            if (valueName !in values) return null
            if (importedModule in visited) return null
            else visited.add(importedModule)
            return local(importedModule) ?: recursive(importedModule)
        }

        private fun importAllBut(importedModule: String, values: Set<String>): ValueDeclarationGroup? {
            if (valueName in values) return null
            if (importedModule in visited) return null
            else visited.add(importedModule)
            return local(importedModule) ?: recursive(importedModule)
        }

        fun importAll(importedModule: String): ValueDeclarationGroup? {
            if (importedModule in visited) return null
            else visited.add(importedModule)
            return local(importedModule) ?: recursive(importedModule)
        }

        private fun recursive(importedModule: String): ValueDeclarationGroup? {
            for (recursiveImport in find.exportedImportsInModule(importedModule)) {
                val export = valueFromImport(recursiveImport)
                if (export != null) return export
            }
            return null
        }


        private fun local(moduleName: String): ValueDeclarationGroup? =
            find.exportedTopLevelValuesInModule(moduleName)
                .firstOrNull { it.name == valueName }


    }

    abstract class ValueIndex(val ver: Int, val indexKey: ValueKey):
        StringStubIndexExtension<ValueDeclarationGroup>(){
        override fun getVersion() = ver
        override fun getKey() = indexKey
    }

    class TopLevelValuesByModule: ValueIndex(0, topLevelValuesByModule)
    class ExportedValuesByModuleName: ValueIndex(0, exportedValuesByModuleName)

    abstract class ImportIndex(val ver: Int, val indexKey: ImportKey):
        StringStubIndexExtension<Import>(){
        override fun getVersion() = ver
        override fun getKey() = indexKey
    }

    class ImportsInModule: ImportIndex(0, importsInModule)
    class ImportsInModuleAndWithAlias: ImportIndex(0, importsInModuleAndWithAlias)
    class ImportsInModuleWithoutAlias: ImportIndex(0, importsInModuleWithoutAlias)
    class ExportedImportsInModule: ImportIndex(0, exportedImportsInModule)

    companion object {
        val topLevelValuesByModule: ValueKey = createIndexKey("purescript.topLevelValueByModules")
        val exportedValuesByModuleName: ValueKey = createIndexKey("purescript.exportedValuesByModuleName")

        val importsInModuleAndWithAlias: ImportKey = createIndexKey("purescript.importsInModuleAndWithAlias")
        val importsInModuleWithoutAlias: ImportKey = createIndexKey("purescript.importsInModuleWithoutAlias")
        val importsInModule: ImportKey = createIndexKey("purescript.importsInModule")
        val exportedImportsInModule: ImportKey = createIndexKey("purescript.exportedImportsInModule")
    }
}