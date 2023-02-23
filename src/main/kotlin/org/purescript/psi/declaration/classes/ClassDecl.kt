package org.purescript.psi.declaration.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.name.PSClassName
import org.purescript.psi.type.PSTypeVarBinding

/**
 * A class declaration, e.g.
 * ```
 * class Foldable f <= FoldableWithIndex i f | f -> i where
 *     foldrWithIndex :: forall a b. (i -> a -> b -> b) -> b -> f a -> b
 *     foldlWithIndex :: forall a b. (i -> b -> a -> b) -> b -> f a -> b
 *     foldMapWithIndex :: forall a m. Monoid m => (i -> a -> m) -> f a -> m
 * ```
 */
class ClassDecl :
    PSStubbedElement<ClassDecl.Stub>,
    PsiNameIdentifierOwner {
    class Stub(val name: String, p: StubElement<*>?) : AStub<ClassDecl>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, ClassDecl>("ClassDecl") {
        override fun createPsi(node: ASTNode) = ClassDecl(node)
        override fun createPsi(stub: Stub) = ClassDecl(stub, this)
        override fun createStub(classDecl: ClassDecl, p: StubElement<*>?) = Stub(classDecl.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)
    internal val classConstraintList: PSClassConstraintList?
        get() = findChildByClass(PSClassConstraintList::class.java)

    internal val className: PSClassName
        get() = findNotNullChildByClass(PSClassName::class.java)

    internal val typeVarBindings: Array<PSTypeVarBinding>
        get() = findChildrenByClass(PSTypeVarBinding::class.java)

    internal val functionalDependencyList: PSClassFunctionalDependencyList?
        get() = findChildByClass(PSClassFunctionalDependencyList::class.java)

    internal val classMemberList: PSClassMemberList?
        get() = findChildByClass(PSClassMemberList::class.java)

    /**
     * @return the [PSClassMember] elements in this declaration,
     * or an empty array if [classConstraintList] is null.
     */
    val classConstraints: Array<PSClassConstraint>
        get() = classConstraintList?.classConstraints ?: emptyArray()

    /**
     * @return the [PSClassMember] elements in this declaration,
     * or an empty array if [classMemberList] is null.
     */
    val classMembers: Array<PSClassMember>
        get() = classMemberList?.classMembers ?: emptyArray()

    override fun getName(): String = className.name

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = className

    override fun getTextOffset(): Int = className.textOffset
}
