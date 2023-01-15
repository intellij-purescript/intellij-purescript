package org.purescript.psi.declaration.newtype

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.name.PSProperName

class NewtypeDecl :
    PSStubbedElement<NewtypeDecl.Stub>, 
    PsiNameIdentifierOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<NewtypeDecl>(p, Type)
    object Type : WithPsiAndStub<Stub, NewtypeDecl>("NewtypeDecl") {
        override fun createPsi(node: ASTNode) = NewtypeDecl(node)
        override fun createPsi(stub: Stub) = NewtypeDecl(stub, this)
        override fun createStub(valueDecl: NewtypeDecl, p: StubElement<*>?) =
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
     * @return the [PSProperName] that identifies this declaration
     */
    private val identifier: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSNewTypeConstructor] defined by this declaration
     */
    val newTypeConstructor: PSNewTypeConstructor
        get() = findNotNullChildByClass(PSNewTypeConstructor::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name

    override fun getTextOffset(): Int = identifier.textOffset
}
