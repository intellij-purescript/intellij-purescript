package org.purescript.psi.module

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

    companion object {
        val KEY = createIndexKey<String, Module>("purescript.module.name")
    }
}