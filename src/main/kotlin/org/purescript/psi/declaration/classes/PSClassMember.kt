package org.purescript.psi.declaration.classes

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentOfType
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.exports.ExportedModule
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.type.PSType
import javax.swing.Icon

/**
 * The a member function of a class declaration, e.g.
 * ```
 * decodeJson :: Json -> Either JsonDecodeError a
 * ```
 * in
 * ```
 * class DecodeJson a where
 *   decodeJson :: Json -> Either JsonDecodeError a
 * ```
 */
class PSClassMember: PSStubbedElement<PSClassMember.Stub>, PsiNameIdentifierOwner, DocCommentOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSClassMember>(p, Type) {
        val module get() = parentStub.parentStub.parentStub as? Module.Stub
        val isExported = when {
            module == null -> false
            module?.exportList == null -> true
            module!!.exportList!!
                .childrenStubs
                .filterIsInstance<ExportedModule.Stub>()
                .any { name == module!!.name }-> true
            else -> module!!.exportList!!
                .childrenStubs
                .filterIsInstance<ExportedValue.Stub>()
                .any { it.name == name}
        }
    }
    object Type : WithPsiAndStub<Stub, PSClassMember>("ClassMember") {
        override fun createPsi(node: ASTNode) = PSClassMember(node)
        override fun createPsi(stub: Stub) = PSClassMember(stub, this)
        override fun createStub(valueDecl: PSClassMember, p: StubElement<*>?) = Stub(valueDecl.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableIndex.key, stub.name)
            }
        }
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    /**
     * @return the [PSIdentifier] identifying this member, e.g.
     * ```
     * decodeJson
     * ```
     * in
     * ```
     * decodeJson :: Json -> Either JsonDecodeError a
     * ```
     */
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun asImport(): ImportDeclaration? = module?.asImport()?.withItems(ImportedValue(name))

    /**
     * @return the [PSType] specifying this member's type signature, e.g.
     * ```
     * Json -> Either JsonDecodeError a
     * ```
     * in
     * ```
     * decodeJson :: Json -> Either JsonDecodeError a
     * ```
     */
    override val type get() = findNotNullChildByClass(PSType::class.java)
    override fun setName(name: String): PsiElement? = null
    override fun getNameIdentifier(): PsiElement = identifier
    override fun getName(): String = greenStub?.name ?: identifier.name
    override val docComments: List<PsiComment> get() = getDocComments().ifEmpty {
        parentOfType<ClassDecl>()?.docComments ?: emptyList()
    }
    override fun getIcon(flags: Int): Icon = AllIcons.Nodes.Function
}
