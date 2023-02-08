package org.purescript.psi.declaration.fixity

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class ExportedFixityNameIndex  : StringStubIndexExtension<FixityDeclaration>() {
    override fun getKey() = KEY

    companion object {
        val KEY = StubIndexKey.createIndexKey<String, FixityDeclaration>(
            "purescript.exported.fixity.decl.name"
        )
    }
}