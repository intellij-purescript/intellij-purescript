package org.purescript.module.declaration.data

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import com.intellij.psi.util.childrenOfType
import org.purescript.ide.formatting.ImportedData
import org.purescript.inference.InferType
import org.purescript.inference.Inferable

import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableTypeIndex
import org.purescript.module.declaration.type.TypeNamespace
import org.purescript.module.declaration.type.TypeParameters
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.exports.ExportedData
import org.purescript.module.exports.ExportedModule
import org.purescript.name.PSProperName
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

/**
 * A data declaration, e.g.
 *
 * ```
 * data CatQueue a = CatQueue (List a) (List a)
 * ```
 */
class DataDeclaration : PSStubbedElement<DataDeclaration.Stub>,
    PsiNameIdentifierOwner,
    Importable,
    TypeNamespace,
    Inferable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<DataDeclaration>(p, Type) {
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

    object Type : PSElementType.WithPsiAndStub<Stub, DataDeclaration>("DataDeclaration") {
        override fun createPsi(node: ASTNode) = DataDeclaration(node)
        override fun createPsi(stub: Stub) = DataDeclaration(stub, this)
        override fun createStub(psi: DataDeclaration, p: StubElement<*>?) = Stub(psi.name, p)
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

    override fun asImport() = module.asImport().withItems(ImportedData(name))
    override val type: PSType? get() = null
    override val typeNames get() = parameters?.typeNames ?: emptySequence()
    val parameters get() = childrenOfType<TypeParameters>().firstOrNull()

    override fun unify() {
        val constructorName = InferType.Constructor(name) as InferType
        val varNames = typeNames.toList()
        val app = varNames.fold(constructorName) { f, arg ->
            f.app(arg.inferType())
        }
        unify(varNames.foldRight(app) { parameter, ret ->
            InferType.function(parameter.inferType(), ret)
        })
    }

    /**
     * @return the [PSProperName] that identifies this declaration
     */
    internal val identifier: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [DataConstructorList.Psi] in this declaration,
     * or null if it's an empty declaration
     */
    internal val dataConstructorList get() = child<DataConstructorList.Psi>()

    /**
     * @return the [DataConstructor] elements belonging to this
     * declaration, or an empty array if it's an empty declaration
     */
    val dataConstructors get() = dataConstructorList?.dataConstructors ?: emptyArray()
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PSProperName = identifier
    override fun getName(): String = greenStub?.name ?: nameIdentifier.name
    override fun getTextOffset(): Int = nameIdentifier.textOffset
    
}