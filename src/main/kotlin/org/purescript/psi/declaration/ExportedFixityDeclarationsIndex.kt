package org.purescript.psi.declaration

import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndexKey
import org.purescript.psi.module.PSModule

class ExportedFixityDeclarationsIndex: StringStubIndexExtension<PSFixityDeclaration>()  {

    override fun getKey(): StubIndexKey<String, PSFixityDeclaration> = KEY

    companion object {
        val KEY =
            StubIndexKey.createIndexKey<String, PSFixityDeclaration>(
                "purescript.exported.fixity.by.module.name"
            )
    }
}