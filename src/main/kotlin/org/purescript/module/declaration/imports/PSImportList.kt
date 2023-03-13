package org.purescript.module.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import org.purescript.parser.HIDING
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

/**
 * The import list in an import declaration.
 *
 * Example:
 * `hiding (foo, bar)`
 *
 * in
 *
 * ```import Foo.Bar hiding (foo, bar) as Bar```
 */
class PSImportList : PSStubbedElement<PSImportList.Stub> {
    class Stub(p: StubElement<*>?) : AStub<PSImportList>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSImportList>("ImportList") {
        override fun createPsi(node: ASTNode) = PSImportList(node)
        override fun createPsi(stub: Stub) = PSImportList(stub, this)
        override fun createStub(my: PSImportList, p: StubElement<*>?) = Stub(p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    private val hidingElement: PsiElement? get() = findChildByType(HIDING)
    /**
     * Returns `true` if the import list is hiding its
     * imported items, `false` otherwise.
     */
    val isHiding: Boolean get() = hidingElement != null

    /**
     * The items that the import list contains. They may be
     * either hidden or exposed depending on whether [isHiding]
     * is `true` or `false`, respectively.
     */
    val importedItems: Array<PSImportedItem> get() = findChildrenByClass(PSImportedItem::class.java)
}
