package org.purescript.psi.module

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import org.purescript.parser.ModuleType

class PSModuleStub(val name: String, parent: StubElement<*>?) : 
    StubBase<Module.PSModule>(parent, ModuleType), StubElement<Module.PSModule> {
    
}