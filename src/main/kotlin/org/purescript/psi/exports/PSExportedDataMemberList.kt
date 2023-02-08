package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import org.purescript.parser.DDOT
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement

/**
 * The exported member list in an [ExportedData.Psi], e.g.
 *
 * ```
 * (Nothing, Just)
 * ```
 * in
 * ```
 * module Data.Maybe (Maybe(Nothing, Just)) where
 * ```
 */
class PSExportedDataMemberList: PSStubbedElement<PSExportedDataMemberList.Stub> {
    class Stub(p: StubElement<*>?) : AStub<PSExportedDataMemberList>(p, Type)
    object Type : WithPsiAndStub<Stub, PSExportedDataMemberList>("ExportedDataMemberList") {
        override fun createPsi(node: ASTNode) = PSExportedDataMemberList(node)
        override fun createPsi(stub: Stub) = PSExportedDataMemberList(stub, this)
        override fun createStub(psi: PSExportedDataMemberList, p: StubElement<*>?) = Stub(p)
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(p)
    }
    constructor(node: ASTNode) : super(node)
    constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
    val doubleDot: PsiElement? get() = findChildByType(DDOT)
    val dataMembers: Array<PSExportedDataMember> get() = findChildrenByClass(PSExportedDataMember::class.java)
}
