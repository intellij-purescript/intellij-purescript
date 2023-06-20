package org.purescript.module.declaration.classes

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
import org.purescript.inference.Inferable
import org.purescript.inference.Scope
import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.exports.ExportedModule
import org.purescript.module.exports.ExportedValue
import org.purescript.name.PSIdentifier
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSStubbedElement
import org.purescript.typechecker.TypeCheckable
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
class PSClassMember: PSStubbedElement<PSClassMember.Stub>,
    PsiNameIdentifierOwner,
    DocCommentOwner,
    Importable,
    TypeCheckable,
    Inferable {
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
    override fun checkReferenceType() = type.checkType()?.addForall()
    override fun infer(scope: Scope): org.purescript.inference.Type = type.infer(scope)
}
