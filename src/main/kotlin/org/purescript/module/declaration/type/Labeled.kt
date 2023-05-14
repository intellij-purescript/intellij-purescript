package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.purescript.name.PSIdentifier
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

class Labeled : PSStubbedElement<Labeled.Stub> {
    class Stub(val label: String, val type: String?, p: StubElement<*>?) : AStub<Labeled>(p, Type) {
    }

    object Type : PSElementType.WithPsiAndStub<Stub, Labeled>("Labeled") {
        override fun createPsi(node: ASTNode) = Labeled(node)
        override fun createPsi(stub: Stub) = Labeled(stub, this)
        override fun createStub(labeled: Labeled, p: StubElement<*>?) = Stub(labeled.name, labeled.typeAsString, p)
        override fun serialize(stub: Stub, d: StubOutputStream) {
            d.writeName(stub.label)
            d.writeName(stub.type)
        }
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, d.readNameString(), p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            sink.occurrence(LabeledIndex.KEY, stub.label)
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName() = greenStub?.label ?: identifier.name
    val type get() = findChildByClass(PSType::class.java)
    val typeAsString get() = greenStub?.type ?: type?.text

}