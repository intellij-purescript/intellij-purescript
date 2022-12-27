package org.purescript.psi.declaration

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.purescript.parser.FixityDeclaration
import org.purescript.parser.Module
import org.purescript.psi.module.PSModule

class PSFixityDeclarationStub(val name: String, parent: StubElement<*>?) :
    StubBase<PSModule>(parent, FixityDeclaration), StubElement<PSModule> {

}