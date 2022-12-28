package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSOperatorName

interface FixityDeclaration {
    object Type : PSElementType.WithPsiAndStub<PSFixityDeclarationStub,
        Psi>("FixityDeclaration") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: PSFixityDeclarationStub) =
            Psi(stub, this)

        override fun createStub(
            psi: Psi,
            parent: StubElement<*>?
        ) =
            PSFixityDeclarationStub(psi.name, parent)

        override fun indexStub(stub: PSFixityDeclarationStub, sink: IndexSink) {
            // if there is a parser error the module might not exist
            stub.getParentStubOfType(Module.Psi::class.java)?.let { module ->
                // TODO only index exported declarations
                sink.occurrence(
                    ExportedFixityDeclarationsIndex.KEY,
                    module.name
                )
            }
        }

        override fun serialize(
            stub: PSFixityDeclarationStub,
            data: StubOutputStream
        ) {
            data.writeName(stub.name)
        }

        override fun deserialize(
            dataStream: StubInputStream,
            parent: StubElement<*>?
        ): PSFixityDeclarationStub =
            PSFixityDeclarationStub(dataStream.readNameString()!!, parent)

    }
    class Psi : PSStubbedElement<PSFixityDeclarationStub>,
        PsiNameIdentifierOwner {

        constructor(node: ASTNode) : super(node)

        constructor(stub: PSFixityDeclarationStub, type: IStubElementType<*, *>)
            : super(stub, type)

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