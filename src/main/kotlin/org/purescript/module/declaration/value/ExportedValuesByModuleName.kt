package org.purescript.module.declaration.value

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object ExportedValuesByModuleName : StringStubIndexExtension<ValueDeclarationGroup>() {
    override fun getKey() = KEY

    val KEY: StubIndexKey<String?, ValueDeclarationGroup?> =
        StubIndexKey.createIndexKey("purescript.exportedValuesByModuleName")
}