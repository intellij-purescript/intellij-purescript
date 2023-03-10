package org.purescript.module.declaration.fixity

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import org.purescript.parser.INFIXL
import org.purescript.parser.INFIXR
import org.purescript.parser.NATURAL
import org.purescript.psi.PSElementType
import org.purescript.psi.AStub
import org.purescript.psi.PSStubbedElement

class PSFixity: PSStubbedElement<PSFixity.Stub> {
    class Stub(p: StubElement<*>?) : AStub<PSFixity>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSFixity>("Fixity") {
        override fun createPsi(node: ASTNode) = PSFixity(node)
        override fun createPsi(stub: Stub) = PSFixity(stub, this)
        override fun createStub(psi: PSFixity, p: StubElement<*>?) = Stub(p)
        override fun indexStub(stub: Stub, sink: IndexSink) {}
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(p)
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
    enum class Associativity {Infixl, Infixr, Infix}
    val associativity: Associativity get() = when {
        findChildByType<PsiElement>(INFIXL) != null-> Associativity.Infixl
        findChildByType<PsiElement>(INFIXR) != null-> Associativity.Infixr
        else -> Associativity.Infix
    }
    val precedence: Int get() = findChildByType<PsiElement>(NATURAL)!!.text.toInt()
}