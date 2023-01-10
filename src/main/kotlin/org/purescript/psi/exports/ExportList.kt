package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.purescript.psi.base.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.PSStubbedElement

/**
 * The export list in the module signature.
 *
 * Example:
 * `(foo, bar)`
 *
 * in
 *
 * ```module Foo.Bar (foo, bar) where```
 */
interface ExportList {
    class Stub(p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportList") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(p)
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(p)
    }

    class Psi : PSStubbedElement<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportList($elementType)"
        val exportedItems = children<ExportedItem<*>>()
    }
}