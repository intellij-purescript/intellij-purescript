package org.purescript.psi.declaration.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.type.PSType

/**
 * The a member function of a class declaration, e.g.
 * ```
 * decodeJson :: Json -> Either JsonDecodeError a
 * ```
 * in
 * ```
 * class DecodeJson a where
 *   decodeJson :: Json -> Either JsonDecodeError a
 * ```
 */
class PSClassMember: PSStubbedElement<PSClassMember.Stub>, PsiNameIdentifierOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSClassMember>(p, Type)
    object Type : WithPsiAndStub<Stub, PSClassMember>("ClassMember") {
        override fun createPsi(node: ASTNode) = PSClassMember(node)
        override fun createPsi(stub: Stub) = PSClassMember(stub, this)
        override fun createStub(valueDecl: PSClassMember, p: StubElement<*>?) = Stub(valueDecl.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    /**
     * @return the [PSIdentifier] identifying this member, e.g.
     * ```
     * decodeJson
     * ```
     * in
     * ```
     * decodeJson :: Json -> Either JsonDecodeError a
     * ```
     */
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)

    /**
     * @return the [PSType] specifying this member's type signature, e.g.
     * ```
     * Json -> Either JsonDecodeError a
     * ```
     * in
     * ```
     * decodeJson :: Json -> Either JsonDecodeError a
     * ```
     */
    val type get() = findNotNullChildByClass(PSType::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PsiElement = identifier
    override fun getName(): String = identifier.name
}
