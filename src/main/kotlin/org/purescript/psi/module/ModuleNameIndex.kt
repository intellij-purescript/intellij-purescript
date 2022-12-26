package org.purescript.psi.module

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

class ModuleNameIndex: StringStubIndexExtension<PSModule>() {

    override fun getKey(): StubIndexKey<String, PSModule> = KEY

    companion object {
        val KEY = createIndexKey<String, PSModule>("purescript.module.name")
    }
}