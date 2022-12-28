package org.purescript.psi.module

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

class ModuleNameIndex: StringStubIndexExtension<Module.Psi>() {

    override fun getKey(): StubIndexKey<String, Module.Psi> = KEY

    companion object {
        val KEY = createIndexKey<String, Module.Psi>("purescript.module.name")
    }
}