package org.purescript.psi.module

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.purescript.parser.Module

class PSModuleStub(val name: String, parent: StubElement<*>?) : 
    StubBase<PSModule>(parent, Module), StubElement<PSModule> {
    
}