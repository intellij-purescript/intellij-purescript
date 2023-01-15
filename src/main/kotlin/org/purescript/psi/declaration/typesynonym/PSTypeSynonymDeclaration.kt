package org.purescript.psi.declaration.typesynonym

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.PSElementType.*
import org.purescript.psi.base.AStub
import org.purescript.psi.name.PSProperName
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.value.ExportedValueDeclNameIndex
import org.purescript.psi.declaration.value.ValueDecl
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.module.Module

/**
 * A type synonym declaration, e.g.
 * ```
 * type GlobalEvents r = ( onContextMenu :: Event | r )
 * ```
 */
class PSTypeSynonymDeclaration :
    PSStubbedElement<PSTypeSynonymDeclaration.Stub>,
    PsiNameIdentifierOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSTypeSynonymDeclaration>(p, Type)
    object Type : WithPsiAndStub<Stub, PSTypeSynonymDeclaration>("TypeSynonymDeclaration") {
        override fun createPsi(node: ASTNode) = PSTypeSynonymDeclaration(node)
        override fun createPsi(stub: Stub) = PSTypeSynonymDeclaration(stub, this)
        override fun createStub(valueDecl: PSTypeSynonymDeclaration, p: StubElement<*>?) =
            Stub(valueDecl.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) :
        super(stub, type)
    /**
     * @return the [PSProperName] identifying this declaration
     */
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name

    override fun getTextOffset(): Int = identifier.textOffset

}
