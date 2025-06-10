package org.purescript.module

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.stubs.StubIndexKey.createIndexKey
import com.intellij.util.Processors

class ModuleNameIndex: StringStubIndexExtension<Module>() {

    override fun getKey(): StubIndexKey<String, Module> = KEY
    fun getAllKeys(scope: GlobalSearchScope): MutableCollection<String> {
        val allKeys: MutableSet<String> = mutableSetOf()
        StubIndex.getInstance().processAllKeys(
            key,
            Processors.cancelableCollectProcessor(allKeys),
            scope,
            null
        )
        return allKeys
    }

    fun getModules(
        name: String,
        project: Project,
        scope: GlobalSearchScope
    ): MutableCollection<Module> = StubIndex.getElements(KEY, name, project, scope, Module::class.java)

    companion object {
        val KEY = createIndexKey<String, Module>("purescript.module.name")
        fun get(name: String, project: Project, scope: GlobalSearchScope): Collection<Module> =
            StubIndex.getElements(KEY, name, project, scope, Module::class.java)

    }
}