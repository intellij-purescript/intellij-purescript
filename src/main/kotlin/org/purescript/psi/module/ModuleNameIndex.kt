package org.purescript.psi.module

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

class ModuleNameIndex: StringStubIndexExtension<Module.PSModule>() {

    override fun getKey(): StubIndexKey<String, Module.PSModule> = KEY

    companion object {
        val KEY = createIndexKey<String, Module.PSModule>("purescript.module.name")
    }
}