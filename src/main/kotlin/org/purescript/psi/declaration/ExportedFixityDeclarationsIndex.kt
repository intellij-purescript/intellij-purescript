package org.purescript.psi.declaration

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

class ExportedFixityDeclarationsIndex: StringStubIndexExtension<FixityDeclaration.Psi>()  {

    override fun getKey(): StubIndexKey<String, FixityDeclaration.Psi> = KEY

    companion object {
        val KEY =
            StubIndexKey.createIndexKey<String, FixityDeclaration.Psi>(
                "purescript.exported.fixity.by.module.name"
            )
    }
}