package org.purescript.psi.declaration.data

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.ide.formatting.ImportedData
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableTypeIndex
import org.purescript.psi.exports.ExportedData
import org.purescript.psi.exports.ExportedModule
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSProperName
import org.purescript.psi.type.PSType

/**
 * A data declaration, e.g.
 *
 * ```
 * data CatQueue a = CatQueue (List a) (List a)
 * ```
 */
interface DataDeclaration {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type) {
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

    object Type : PSElementType.WithPsiAndStub<Stub, Psi>("DataDeclaration") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableTypeIndex.KEY, stub.name)
            }
        }
    }

    class Psi : PSStubbedElement<Stub>, PsiNameIdentifierOwner, Importable {
        constructor(node: ASTNode) : super(node)
        constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

        override fun asImport() = module?.asImport()?.withItems(ImportedData(name))
        override val type: PSType? get() = null

        // Todo clean this up
        override fun toString(): String = "PSDataDeclaration($elementType)"

        /**
         * @return the [PSProperName] that identifies this declaration
         */
        internal val identifier: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)

        /**
         * @return the [DataConstructorList.Psi] in this declaration,
         * or null if it's an empty declaration
         */
        internal val dataConstructorList get() = findChildByClass(DataConstructorList.Psi::class.java)

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
}

