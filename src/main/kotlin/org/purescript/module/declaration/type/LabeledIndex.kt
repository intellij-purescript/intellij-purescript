package org.purescript.module.declaration.type

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object LabeledIndex : StringStubIndexExtension<Labeled>() {
    override fun getKey() = KEY

    val KEY = StubIndexKey.createIndexKey<String, Labeled>(
        "purescript.labeled.index"
    )
}