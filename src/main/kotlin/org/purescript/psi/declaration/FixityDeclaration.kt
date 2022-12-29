package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSOperatorName

interface FixityDeclaration {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)

    object Type : WithPsiAndStub<Stub, Psi>("FixityDeclaration") {
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
        override fun toString(): String = "PSFixityDeclaration($elementType)"

        private val operatorName
            get() = findNotNullChildByClass(PSOperatorName::class.java)

        override fun getTextOffset(): Int = nameIdentifier.textOffset
        override fun getNameIdentifier() = operatorName
        override fun getName() = stub?.name ?: operatorName.name
        override fun setName(name: String): PsiElement? {
            val identifier = PSPsiFactory(project).createOperatorName(name)
                ?: return null
            nameIdentifier.replace(identifier)
            return this
        }
    }
}