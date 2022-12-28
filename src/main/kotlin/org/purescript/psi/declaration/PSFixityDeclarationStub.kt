package org.purescript.psi.declaration

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

class PSFixityDeclarationStub(val name: String, parent: StubElement<*>?) :
    StubBase<FixityDeclaration.Psi>(parent, FixityDeclaration.Type),
    StubElement<FixityDeclaration.Psi> {

}