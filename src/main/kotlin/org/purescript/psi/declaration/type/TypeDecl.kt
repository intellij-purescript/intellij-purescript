package org.purescript.psi.declaration.type

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedData
import org.purescript.parser.ImportedData
import org.purescript.psi.PSElementType.*
import org.purescript.psi.base.AStub
import org.purescript.psi.name.PSProperName
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.Importable
import org.purescript.psi.type.PSType

/**
 * A type synonym declaration, e.g.
 * ```
 * type GlobalEvents r = ( onContextMenu :: Event | r )
 * ```
 */
class TypeDecl : PSStubbedElement<TypeDecl.Stub>, PsiNameIdentifierOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<TypeDecl>(p, Type)
    object Type : WithPsiAndStub<Stub, TypeDecl>("TypeDecl") {
        override fun createPsi(node: ASTNode) = TypeDecl(node)
        override fun createPsi(stub: Stub) = TypeDecl(stub, this)
        override fun createStub(valueDecl: TypeDecl, p: StubElement<*>?) =
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
     * @return the [PSProperName] identifying this declaration
     */
    private val identifier: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PsiElement = identifier
    override fun getName(): String = identifier.name
    override fun asImport() = module?.asImport()?.withItems(ImportedData(name))
    override val type get() = null
    override fun getTextOffset(): Int = identifier.textOffset
}
