package org.purescript.module.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentOfType
import org.purescript.name.PSProperName
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

/**
 * An exported data member in an [ExportedData.Psi], e.g.
 *
 * ```
 * Nothing
 * ```
 * in
 * ```
 * module Data.Maybe (Maybe(Nothing)) where
 * ```
 */
class PSExportedDataMember: PSStubbedElement<PSExportedDataMember.Stub> {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSExportedDataMember>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSExportedDataMember>("ExportedDataMember") {
        override fun createPsi(node: ASTNode) = PSExportedDataMember(node)
        override fun createPsi(stub: Stub) = PSExportedDataMember(stub, this)
        override fun createStub(psi: PSExportedDataMember, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }
    constructor(node: ASTNode) : super(node)
    constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
    /**
     * @return the identifier of this element
     */
    internal val properName: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [ExportedData.Psi] element containing this member
     */
    internal val exportedData: ExportedData.Psi? get() = parentOfType()
    override fun getName(): String = properName.name
    override fun getReference(): PsiReference = ExportedDataMemberReference(this)
}
