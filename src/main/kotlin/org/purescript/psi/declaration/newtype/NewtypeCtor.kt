package org.purescript.psi.declaration.newtype

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedData
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.name.PSProperName
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.exports.ExportedData
import org.purescript.psi.exports.ExportedModule
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.module.Module
import org.purescript.psi.type.PSType
import javax.swing.Icon

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
    PsiNameIdentifierOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<NewtypeCtor>(p, Type) {
        val module get() = parentStub.parentStub as? Module.Stub
        val isExported get() = when {
            module == null -> false
            module?.exportList == null -> true
            module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedModule.Stub>()
                ?.find { it.name == module?.name }!= null -> true
            else -> module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedData.Stub>()
                ?.find { exportedData ->
                    exportedData.name == name &&
                            exportedData.dataMembers.run { isEmpty() || any { it.name == name } }
                } != null
        }
    }
    object Type : PSElementType.WithPsiAndStub<Stub, NewtypeCtor>("NewtypeCtor") {
        override fun createPsi(node: ASTNode) = NewtypeCtor(node)
        override fun createPsi(stub: Stub) = NewtypeCtor(stub, this)
        override fun createStub(valueDecl: NewtypeCtor, p: StubElement<*>?) =
            Stub(valueDecl.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableIndex.KEY, stub.name)
            }
        }
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
    /**
     * @return the [PSProperName] identifying this constructor
     */
    internal val identifier: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getName(): String = nameIdentifier.name
    override fun getNameIdentifier(): PSProperName = identifier
    override fun asImport() = module?.asImport()?.withItems(ImportedData(name, dataMembers = setOf(name)))
    override val type: PSType? get() = null
    override fun getIcon(flags: Int) = AllIcons.Nodes.Class
}
