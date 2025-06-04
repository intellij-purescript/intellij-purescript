package org.purescript.module.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.psi.util.childrenOfType
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSStubbedElement

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
class ExportList : PSStubbedElement<ExportList.Stub> {
    class Stub(p: StubElement<*>?) : AStub<ExportList>(p, Type)
    object Type : WithPsiAndStub<Stub, ExportList>("ExportList") {
        override fun createPsi(node: ASTNode) = ExportList(node)
        override fun createPsi(stub: Stub) = ExportList(stub, this)
        override fun createStub(psi: ExportList, p: StubElement<*>?) = Stub(p)
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(p)
    }
    constructor(node: ASTNode) : super(node)
    constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
    // Todo clean this up
    override fun toString(): String = "PSExportList($elementType)"
    val values: List<ExportedValue.Psi> get() = childrenOfType<ExportedValue.Psi>()
    val modules: List<ExportedModule> get() = childrenOfType<ExportedModule>()
    val exportedItems = children<ExportedItem<*>>()
}