package org.purescript.psi.declaration.newtype

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.name.PSProperName
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.base.PSStubbedElement

/**
 * A constructor in a newtype declaration, e.g.
 *
 * ```
 * CatQueue (List a) (List a)
 * ```
 * in
 * ```
 * newtype CatQueue a = CatQueue (List a) (List a)
 * ```
 */
class NewtypeCtor :
    PSStubbedElement<NewtypeCtor.Stub>,
    PsiNameIdentifierOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<NewtypeCtor>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, NewtypeCtor>("NewtypeCtor") {
        override fun createPsi(node: ASTNode) = NewtypeCtor(node)
        override fun createPsi(stub: Stub) = NewtypeCtor(stub, this)
        override fun createStub(valueDecl: NewtypeCtor, p: StubElement<*>?) =
            Stub(valueDecl.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
    /**
     * @return the [PSProperName] identifying this constructor
     */
    internal val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PSProperName = identifier

    override fun getName(): String = identifier.name
}
