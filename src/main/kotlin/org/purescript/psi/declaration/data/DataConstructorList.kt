package org.purescript.psi.declaration.data

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement

/**
 * The data constructors in a data declaration, e.g.
 *
 * ```
 * = Left a | Right b
 * ```
 * in
 * ```
 * data Either a b = Left a | Right b
 * ```
 */
interface DataConstructorList{

    class Stub(p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, Psi>("DataConstructorList") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) =
            Stub(p)

        override fun indexStub(stub: Stub, sink: IndexSink) = Unit

        override fun serialize(stub: Stub, d: StubOutputStream) = Unit

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(p)
    }

    class Psi : PSStubbedElement<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(stub: Stub, type: IStubElementType<*, *>) :
            super(stub, type)

        // Todo clean this up
        override fun toString(): String = "PSDataConstructorList($elementType)"
        /**
         * @return the [DataConstructor] elements in this list
         */
        internal val dataConstructors: Array<DataConstructor>
            get() = findChildrenByClass(DataConstructor::class.java)
    }
}