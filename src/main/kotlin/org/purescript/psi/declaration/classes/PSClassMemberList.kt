package org.purescript.psi.declaration.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement

/**
 * The members part of a class declaration, e.g.
 * ```
 * where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 * in
 * ```
 * class Foldable f <= FoldableWithIndex i f | f -> i where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 */
class PSClassMemberList: PSStubbedElement<PSClassMemberList.Stub> {
    class Stub(p: StubElement<*>?) : AStub<PSClassMemberList>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSClassMemberList>("ClassMemberList") {
        override fun createPsi(node: ASTNode) = PSClassMemberList(node)
        override fun createPsi(stub: Stub) = PSClassMemberList(stub, this)
        override fun createStub(valueDecl: PSClassMemberList, p: StubElement<*>?) = Stub(p)
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
    /**
     * @return the [PSClassMember] elements contained in this list
     */
    val classMembers: Array<PSClassMember>
        get() = findChildrenByClass(PSClassMember::class.java)
}
