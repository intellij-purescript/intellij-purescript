package org.purescript.module.declaration.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.AStub
import org.purescript.psi.PSStubbedElement

class PSInstanceDeclaration :
    PSStubbedElement<PSInstanceDeclaration.Stub> {
    class Stub(p: StubElement<*>?) : AStub<PSInstanceDeclaration>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSInstanceDeclaration>("TypeInstanceDeclaration") {
        override fun createPsi(node: ASTNode) = PSInstanceDeclaration(node)
        override fun createPsi(stub: Stub) = PSInstanceDeclaration(stub, this)
        override fun createStub(valueDecl: PSInstanceDeclaration, p: StubElement<*>?) = Stub(p)
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
}