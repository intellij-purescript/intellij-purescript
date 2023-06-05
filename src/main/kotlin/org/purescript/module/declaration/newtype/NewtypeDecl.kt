package org.purescript.module.declaration.newtype

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import com.intellij.psi.util.childrenOfType
import org.purescript.ide.formatting.ImportedData
import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.declaration.type.TypeNamespace
import org.purescript.module.declaration.type.TypeParameters
import org.purescript.module.exports.ExportedData
import org.purescript.module.exports.ExportedModule
import org.purescript.name.PSProperName
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSStubbedElement

class NewtypeDecl : PSStubbedElement<NewtypeDecl.Stub>, PsiNameIdentifierOwner, Importable, TypeNamespace {
    class Stub(val name: String, p: StubElement<*>?) : AStub<NewtypeDecl>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported get() = when {
            module == null -> false
            module?.exportList == null -> true
            module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedModule.Stub>()
                ?.find { it.name == module?.name }!= null -> true
            else -> module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedData.Stub>()
                ?.find { exportedData -> exportedData.name == name } != null
        }
    }

    object Type : WithPsiAndStub<Stub, NewtypeDecl>("NewtypeDecl") {
        override fun createPsi(node: ASTNode) = NewtypeDecl(node)
        override fun createPsi(stub: Stub) = NewtypeDecl(stub, this)
        override fun createStub(valueDecl: NewtypeDecl, p: StubElement<*>?) = Stub(valueDecl.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if(stub.isExported) {
                sink.occurrence(ImportableTypeIndex.KEY, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    /**
     * @return the [PSProperName] that identifies this declaration
     */
    private val identifier get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [NewtypeCtor] defined by this declaration
     */
    val newTypeConstructor get() = child<NewtypeCtor>()!!
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PsiElement = identifier
    override fun getName(): String = greenStub?.name ?: identifier.name
    override fun asImport() = module?.asImport()?.withItems(ImportedData(name))
    override val type: PSType? get() = null
    override val typeNames get() = parameters?.typeNames ?: emptySequence()
    val parameters get() = childrenOfType<TypeParameters>().firstOrNull()
    override fun getTextOffset(): Int = identifier.textOffset
}
