package org.purescript.psi.declaration.value

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object ExportedValueDecl : StringStubIndexExtension<ValueDeclarationGroup>() {
    override fun getKey() = KEY

    val KEY = StubIndexKey.createIndexKey<String, ValueDeclarationGroup>(
        "purescript.exported.value"
    )
}