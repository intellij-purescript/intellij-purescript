package org.purescript.module.declaration.newtype

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentOfType
import org.purescript.ide.formatting.ImportedData
import org.purescript.inference.InferType
import org.purescript.inference.Inferable
import org.purescript.module.Module
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.exports.ExportedData
import org.purescript.module.exports.ExportedModule
import org.purescript.name.PSProperName
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

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
    PsiNameIdentifierOwner, org.purescript.module.declaration.Importable,
    Inferable{
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
        override fun createStub(valueDecl: NewtypeCtor, p: StubElement<*>?) = Stub(valueDecl.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
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
    override fun getName(): String = greenStub?.name ?: nameIdentifier.name
    override fun getNameIdentifier(): PSProperName = identifier
    override fun asImport() = module?.asImport()?.withItems(ImportedData(name, dataMembers = setOf(name)))
    override val type: PSType? get() = null
    val typeAtom: PSType get() = findChildByClass(PSType::class.java) ?: error("newtype constructor without type")
    override fun getIcon(flags: Int) = AllIcons.Nodes.Class
    val newTypeDeclaration get() = parentOfType<NewtypeDecl>() ?: error("newtype constructor without newtype declaration")
    override fun unify() {
        val typeNames = newTypeDeclaration.typeNames.toList()
        val type = InferType.Constructor(name).app(typeAtom.inferType())
        val function = typeNames.foldRight(type) { parameter, ret ->
            InferType.function(parameter.inferType(), ret)
        }
        unify(function)
    }
}
