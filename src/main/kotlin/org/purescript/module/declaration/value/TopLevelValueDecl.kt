package org.purescript.module.declaration.value

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object TopLevelValueDecl : StringStubIndexExtension<ValueDeclarationGroup>() {
    override fun getKey() = KEY

    val KEY = StubIndexKey.createIndexKey<String, ValueDeclarationGroup>(
        "purescript.toplevel.value"
    )
}