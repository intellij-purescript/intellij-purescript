package org.purescript.psi.declaration

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object ImportableTypeIndex : StringStubIndexExtension<Importable>() {
    override fun getKey() = KEY

    val KEY = StubIndexKey.createIndexKey<String, Importable>(
        "purescript.importable.type"
    )
}