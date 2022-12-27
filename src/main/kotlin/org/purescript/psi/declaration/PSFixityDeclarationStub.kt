package org.purescript.psi.declaration

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.purescript.parser.FixityDeclaration

class PSFixityDeclarationStub(val name: String, parent: StubElement<*>?) :
    StubBase<PSFixityDeclaration>(parent, FixityDeclaration),
    StubElement<PSFixityDeclaration> {

}