package org.purescript.module

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.NavigationItem
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

class ModuleChooseByNameContributor: ChooseByNameContributorEx {
    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        val project = scope.project ?: return
        val index = ModuleNameIndex()
        for (key in index.getAllKeys(scope)) {
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
        val modules = ModuleNameIndex().getModules(name, project, scope)
        for (module in modules) {
            if (!processor.process(module)) {
                return
            }
        }
    }

}