package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.ide.formatting.ImportedData
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.AStub
import org.purescript.psi.PSStubbedElement
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.exports.ExportedData
import org.purescript.module.exports.ExportedModule
import org.purescript.module.Module
import org.purescript.name.PSProperName

/**
 * A type synonym declaration, e.g.
 * ```
 * type GlobalEvents r = ( onContextMenu :: Event | r )
 * ```
 */
class TypeDecl : PSStubbedElement<TypeDecl.Stub>, PsiNameIdentifierOwner,
    org.purescript.module.declaration.Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<TypeDecl>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported
            get() = when {
                module == null -> false
                module?.exportList == null -> true
                module?.exportList?.childrenStubs
                    ?.filterIsInstance<ExportedModule.Stub>()
                    ?.find { it.name == module?.name } != null -> true

                else -> module?.exportList?.childrenStubs
                    ?.filterIsInstance<ExportedData.Stub>()
                    ?.find { exportedData -> exportedData.name == name } != null
            }
    }
    object Type : WithPsiAndStub<Stub, TypeDecl>("TypeDecl") {
        override fun createPsi(node: ASTNode) = TypeDecl(node)
        override fun createPsi(stub: Stub) = TypeDecl(stub, this)
        override fun createStub(valueDecl: TypeDecl, p: StubElement<*>?) =
            Stub(valueDecl.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableTypeIndex.KEY, stub.name)
            }
        }
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
    /**
     * @return the [PSProperName] identifying this declaration
     */
    private val identifier get() = findNotNullChildByClass(PSProperName::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PsiElement = identifier
    override fun getName(): String = greenStub?.name ?: identifier.name
    override fun asImport() = module?.asImport()?.withItems(ImportedData(name))
    override val type get() = null
    override fun getTextOffset(): Int = identifier.textOffset
}
