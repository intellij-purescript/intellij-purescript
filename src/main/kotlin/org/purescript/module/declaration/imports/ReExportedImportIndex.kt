package org.purescript.module.declaration.imports

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey.createIndexKey

/**
 * Maps Module names to Import Statements that imports that module and also are exported by their own module
 */
object ReExportedImportIndex : StringStubIndexExtension<Import>() {
    val KEY = createIndexKey<String, Import>("purescript.reexported.import")

    override fun getKey() = KEY
}