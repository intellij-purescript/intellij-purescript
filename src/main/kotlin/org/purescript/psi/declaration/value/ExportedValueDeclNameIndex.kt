package org.purescript.psi.declaration.value

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class ExportedValueDeclNameIndex : StringStubIndexExtension<ValueDecl>() {
    override fun getKey() = KEY

    companion object {
        val KEY = StubIndexKey.createIndexKey<String, ValueDecl>(
            "purescript.exported.value.decl.name"
        )
    }
}