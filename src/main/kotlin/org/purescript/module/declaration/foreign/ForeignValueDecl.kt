package org.purescript.module.declaration.foreign

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import com.intellij.psi.util.childrenOfType
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.exports.ExportedValue
import org.purescript.name.PSIdentifier
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

/**
 * A foreign value import declaration, e.g.
 *
 * ```
 * foreign import xor :: Int -> Int -> Int
 * ```
 */
class ForeignValueDecl : PSStubbedElement<ForeignValueDecl.Stub>,
    PsiNameIdentifierOwner,
    DocCommentOwner,
    Importable,
    Inferable {
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
        override fun createStub(my: ForeignValueDecl, p: StubElement<*>?) = Stub(my.name, p)
        override fun serialize(my: Stub, d: StubOutputStream) = d.writeName(my.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ExportedForeignValueDeclIndex.KEY, stub.name)
                sink.occurrence(ImportableIndex.KEY, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    override fun getIcon(flags: Int) = AllIcons.Ide.External_link_arrow
    override fun setName(name: String) = null
    override fun getNameIdentifier() = findChildByClass(PSIdentifier::class.java)!!
    override fun asImport() = module?.name?.let { ImportDeclaration(it).withItems(ImportedValue(name)) }
    override val type: PSType? get() = childrenOfType<PSType>().firstOrNull()
    override fun getName() = greenStub?.name ?: nameIdentifier.name
    override val docComments: List<PsiComment> get() = this.getDocComments()
    override fun infer(scope: Scope): org.purescript.inference.InferType = type!!.infer(scope)
}
