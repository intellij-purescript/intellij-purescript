package org.purescript.psi.declaration.value

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.psi.Importable
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.module.Module

class ValueDeclarationGroup: PSStubbedElement<ValueDeclarationGroup.Stub>,
    PsiNameIdentifierOwner, DocCommentOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<ValueDeclarationGroup>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported get() = when {
            module == null -> false
            module?.exportList == null -> true
            else -> module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedValue.Stub>()
                ?.find { it.name == name } != null
        }
    }

    object Type : PSElementType.WithPsiAndStub<Stub, ValueDeclarationGroup>("ValueDeclarationGroup") {
        override fun createPsi(node: ASTNode) = ValueDeclarationGroup(node)
        override fun createPsi(stub: Stub) = ValueDeclarationGroup(stub, this)
        override fun createStub(me: ValueDeclarationGroup, p: StubElement<*>?) =
            Stub(me.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {}
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    val signature: PSSignature? get() = findChildByClass(PSSignature::class.java)
    val valueDeclarations: Array<out ValueDecl> get() = 
        findChildrenByClass(ValueDecl::class.java)
    val expressionAtoms get() = valueDeclarations.flatMap { it.expressionAtoms }
    
    override fun setName(name: String): PsiElement {
        for (valueDeclaration in valueDeclarations) {
            valueDeclaration.setName(name)
        }
        return this
    }

    override fun getName(): String {
        return valueDeclarations.first().name
    }

    override fun getNameIdentifier(): PsiElement? =
        valueDeclarations.firstOrNull()?.nameIdentifier

    override val docComments: List<PsiComment>
        get() = this.getDocComments() + valueDeclarations.flatMap { it.docComments }.toList()

    override fun asImport(): ImportDeclaration? {
        return valueDeclarations.firstOrNull()?.asImport()
    }
}