package org.purescript.module.declaration.foreign

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object ExportedForeignValueDeclIndex : StringStubIndexExtension<ForeignValueDecl>() {
    override fun getKey() = KEY

    val KEY = StubIndexKey.createIndexKey<String, ForeignValueDecl>(
        "purescript.exported.foreign.value.decl.name"
    )
}