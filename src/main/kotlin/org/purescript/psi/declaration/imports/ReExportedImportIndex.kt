package org.purescript.psi.declaration.imports

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

object ReExportedImportIndex : StringStubIndexExtension<Import>() {
    val KEY = createIndexKey<String, Import>("purescript.reexported.import")

    override fun getKey() = KEY
}