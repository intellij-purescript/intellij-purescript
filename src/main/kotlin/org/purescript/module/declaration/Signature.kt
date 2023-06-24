package org.purescript.module.declaration

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.stubs.*
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.module.declaration.type.type.PSType
import org.purescript.name.PSIdentifier
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement

/**
 * `foo :: int` in
 * ```
 * foo :: Int
 * foo = 42
 * ```
 */
class Signature :
    PSStubbedElement<Signature.Stub>,
    Inferable {

    class Stub(val name: String, p: StubElement<*>?) : AStub<Signature>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, Signature>("Signature") {
        override fun createPsi(node: ASTNode) = Signature(node)
        override fun createPsi(stub: Stub) = Signature(stub, this)
        override fun createStub(me: Signature, p: StubElement<*>?) = Stub(me.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {}
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    val type get() = findNotNullChildByClass(PSType::class.java)
    override fun getName() = identifier.name
    val nameIdentifier: PSIdentifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    fun setName(name: String) {
        val identifier =
            project.service<PSPsiFactory>().createIdentifier(name)
                ?: return
        nameIdentifier.replace(identifier)
    }

    override fun getReference(): PsiReference? =
        parent?.let { PsiReferenceBase.Immediate(this, nameIdentifier.textRangeInParent, it) }
    override fun infer(scope: Scope): org.purescript.inference.InferType {
        return type.infer(scope)
    }
}