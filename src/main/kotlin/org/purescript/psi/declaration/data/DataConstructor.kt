package org.purescript.psi.declaration.data

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.name.PSProperName
import org.purescript.psi.type.PSTypeAtom

/**
 * A data constructor in a data declaration, e.g.
 *
 * ```
 * CatQueue (List a) (List a)
 * ```
 * in
 * ```
 * data CatQueue a = CatQueue (List a) (List a)
 * ```
 */
class DataConstructor : PSStubbedElement<DataConstructor.Stub>, PsiNameIdentifierOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<DataConstructor>(p, Type)
    object Type : WithPsiAndStub<Stub, DataConstructor>("DataConstructor") {
        override fun createPsi(node: ASTNode) = DataConstructor(node)
        override fun createPsi(stub: Stub) = DataConstructor(stub, this)
        override fun createStub(psi: DataConstructor, p: StubElement<*>?) = Stub(psi.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    // Todo clean this up
    override fun toString(): String = "PSDataConstructor($elementType)"

    /**
     * @return the [PSProperName] identifying this constructor
     */
    internal val identifier: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSTypeAtom] elements in this constructor
     */
    internal val typeAtoms: Array<PSTypeAtom> get() = findChildrenByClass(PSTypeAtom::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PSProperName = identifier
    override fun getName(): String = identifier.name
}

