package org.purescript.psi.declaration.foreign

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSIdentifier

/**
 * A foreign value import declaration, e.g.
 *
 * ```
 * foreign import xor :: Int -> Int -> Int
 * ```
 */
class ForeignValueDecl : PSStubbedElement<ForeignValueDecl.Stub>,
    PsiNameIdentifierOwner, DocCommentOwner {
    class Stub(val name: String, p: StubElement<*>?) :
        AStub<ForeignValueDecl>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported
            get() = when {
                module == null -> false
                module?.exportList == null -> true
                else -> module?.exportList?.childrenStubs
                    ?.filterIsInstance<ExportedValue.Stub>()
                    ?.find { it.name == name } != null
            }
    }

    object Type :
        PSElementType.WithPsiAndStub<Stub, ForeignValueDecl>("ForeignValueDecl") {
        override fun createPsi(node: ASTNode) = ForeignValueDecl(node)
        override fun createPsi(stub: Stub) = ForeignValueDecl(stub, this)
        override fun createStub(my: ForeignValueDecl, p: StubElement<*>?) =
            Stub(my.name, p)

        override fun serialize(my: Stub, d: StubOutputStream) =
            d.writeName(my.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                val key: StubIndexKey<String, ForeignValueDecl> = ExportedForeignValueDeclIndex.KEY
                sink.occurrence(key, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    override fun setName(name: String) = null
    override fun getNameIdentifier() =
        findChildByClass(PSIdentifier::class.java)!!

    override fun getName() = nameIdentifier.name
    override val docComments: List<PsiComment>
        get() = this.getDocComments()
}
