package org.purescript.graph

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.psi.declaration.imports.ReExportedImportIndex

@Service
class ImportGraph(val project: Project) {

    fun foo(name: String): Map<String, List<Module>> {
        val scope = GlobalSearchScope.allScope(project)
        val names = mutableListOf(name)
        val graph: MutableMap<String, List<Module>> = mutableMapOf()
        while (names.isNotEmpty()) {
            val currentName  = names.removeFirst()
            if (currentName in graph) continue
            val fromIndex = ReExportedImportIndex.get(currentName, project, scope)
            val modules: List<Module> = fromIndex
                .groupBy { it.module?.name }
                .mapNotNull { (moduleName, imports) ->
                    moduleName?.let { name -> Module(name, imports.map { ImportDeclaration.fromPsiElement(it) }) }
                }
            graph[name] = modules
            names.addAll(modules.map { it.name })
        }
        return graph
    }
}