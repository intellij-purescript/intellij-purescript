package org.purescript.psi.declaration.data

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.name.PSProperName
import org.purescript.psi.base.PSStubbedElement

/**
 * A data declaration, e.g.
 *
 * ```
 * data CatQueue a = CatQueue (List a) (List a)
 * ```
 */
interface DataDeclaration {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, Psi>("DataDeclaration") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) =
            Stub(psi.name, p)

        override fun indexStub(stub: Stub, sink: IndexSink) = Unit

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)
    }

    class Psi : PSStubbedElement<Stub>, PsiNameIdentifierOwner {
        constructor(node: ASTNode) : super(node)
        constructor(stub: Stub, type: IStubElementType<*, *>) :
            super(stub, type)

        // Todo clean this up
        override fun toString(): String = "PSDataDeclaration($elementType)"

        /**
         * @return the [PSProperName] that identifies this declaration
         */
        internal val identifier: PSProperName
            get() = findNotNullChildByClass(PSProperName::class.java)

        /**
         * @return the [DataConstructorList.Psi] in this declaration,
         * or null if it's an empty declaration
         */
        internal val dataConstructorList: DataConstructorList.Psi?
            get() = findChildByClass(DataConstructorList.Psi::class.java)

        /**
         * @return the [DataConstructor.Psi] elements belonging to this
         * declaration, or an empty array if it's an empty declaration
         */
        val dataConstructors: Array<DataConstructor.Psi>
            get() = dataConstructorList?.dataConstructors
                ?: emptyArray()

        override fun setName(name: String): PsiElement? {
            return null
        }

        override fun getNameIdentifier(): PSProperName = identifier

        override fun getName(): String = nameIdentifier.name

        override fun getTextOffset(): Int = nameIdentifier.textOffset
    }
}

