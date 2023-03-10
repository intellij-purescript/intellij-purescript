package org.purescript.module.declaration.foreign

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportedData
import org.purescript.module.Module
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.type.PSType
import org.purescript.module.exports.ExportedData
import org.purescript.module.exports.ExportedModule
import org.purescript.name.PSProperName
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

/**
 * A foreign data declaration, e.g.
 * ```
 * foreign import data Effect :: Type -> Type
 * ```
 */
class PSForeignDataDeclaration :
    PSStubbedElement<PSForeignDataDeclaration.Stub>,
    PsiNameIdentifierOwner, DocCommentOwner, org.purescript.module.declaration.Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSForeignDataDeclaration>(p, Type) {
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
    object Type : PSElementType.WithPsiAndStub<Stub, PSForeignDataDeclaration>("ForeignDataDeclaration") {
        override fun createPsi(node: ASTNode) = PSForeignDataDeclaration(node)
        override fun createPsi(stub: Stub) = PSForeignDataDeclaration(stub, this)
        override fun createStub(psi: PSForeignDataDeclaration, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableTypeIndex.KEY, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    override fun asImport() = module?.asImport()?.withItems(ImportedData(name))
    override val type: PSType? get() = null
    internal val properName: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PsiElement = properName
    override fun getName(): String = greenStub?.name ?: properName.name
    override fun getTextOffset(): Int = properName.textOffset
    override val docComments: List<PsiComment> get() = getDocComments()
}
