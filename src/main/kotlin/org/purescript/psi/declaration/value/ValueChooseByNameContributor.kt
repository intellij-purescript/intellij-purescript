package org.purescript.psi.declaration.value

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

/**
 * Allows to search anywhere for toplevel names
 */
class ValueChooseByNameContributor: ChooseByNameContributorEx {
    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        val project = scope.project ?: return
        val index = TopLevelValueDecl
        val keys = index.getAllKeys(project)
        for (key in keys) {
            val modules = index.get(key, project, scope)
            if (modules.isNotEmpty()) {
                if (!processor.process(key)) {
                    return
                }
            }
        }
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters
    ) {
        val project = parameters.project
        val scope = parameters.searchScope
        val index = TopLevelValueDecl
        val modules = index.get(name, project, scope)
        for (module in modules) {
            if (!processor.process(module)) {
                return
            }
        }
    }
}