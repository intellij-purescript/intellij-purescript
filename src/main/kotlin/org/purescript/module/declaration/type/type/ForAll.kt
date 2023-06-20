package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.purescript.inference.Scope
import org.purescript.module.declaration.type.TypeVarName
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement
import org.purescript.typechecker.TypeCheckerType

class ForAll: PSStubbedElement<ForAll.Stub>, PSType {
    class Stub( p: StubElement<*>?) : AStub<ForAll>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, ForAll>("ForAll") {
        override fun createPsi(node: ASTNode) = ForAll(node)
        override fun createPsi(stub: Stub) = ForAll(stub, this)
        override fun createStub(me: ForAll, p: StubElement<*>?) = Stub(p)
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(p)
        override fun indexStub(stub: Stub, sink: IndexSink) {}
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    private val type get() = findChildByClass(PSType::class.java)
    private val typeVars get() = findChildrenByClass(TypeVarName::class.java)
    override fun checkType(): TypeCheckerType? {
        return typeVars
            .mapNotNull { it.name }
            .fold(type?.checkType() ?: return null) { acc: TypeCheckerType, name: String ->
                TypeCheckerType.ForAll(name, acc)
            }
    }

    override fun infer(scope: Scope): org.purescript.inference.Type {
        TODO("Not yet implemented")
    }
}