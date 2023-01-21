package org.purescript.psi.declaration.imports

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

class ReExportedImportIndex : StringStubIndexExtension<Import>() {
    override fun getKey() = KEY

    companion object {
        val KEY = createIndexKey<String, Import>("purescript.reexported.import")
    }
}