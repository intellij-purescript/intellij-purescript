package org.purescript.module.declaration.type

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentOfType
import org.purescript.inference.Inferable
import org.purescript.name.PSIdentifier
import org.purescript.psi.*

sealed interface PSTypeVarBinding: Inferable

class TypeVarName : PSStubbedElement<TypeVarName.Stub>,
    PSTypeVarBinding,
    PsiNameIdentifierOwner {
    class Stub(val name:String, p: StubElement<*>?) : AStub<TypeVarName>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, TypeVarName>("TypeVarName") {
        override fun createPsi(node: ASTNode) = TypeVarName(node)
        override fun createPsi(stub: Stub) = TypeVarName(stub, this)
        override fun createStub(me: TypeVarName, p: StubElement<*>?) = Stub(me.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {}
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getNameIdentifier() = identifier
    override fun getName(): String = identifier.name
    override fun getUseScope() = LocalSearchScope(parentOfType<TypeNamespace>(withSelf = false) ?: containingFile)
    override fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val identifier = factory.createIdentifier(name) ?: return null
        nameIdentifier.replace(identifier)
        return this
    }
    override fun unify() {}
}

class PSTypeVarKinded(node: ASTNode) : PSPsiElement(node), PSTypeVarBinding {
    override fun unify() {}
}
