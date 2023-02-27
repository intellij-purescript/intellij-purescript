package org.purescript.psi.declaration.fixity

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.components.service
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedOperator
import org.purescript.parser.TYPE
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.declaration.classes.PSClassMember
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.exports.ExportedOperator
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSOperatorName
import org.purescript.psi.name.PSQualifiedIdentifier
import org.purescript.psi.name.PSQualifiedProperName
import org.purescript.psi.type.PSType

class FixityDeclaration : PSStubbedElement<FixityDeclaration.Stub>, PsiNameIdentifierOwner, Importable,
    DocCommentOwner {
    class Stub(val name: String, p: StubElement<*>?) :
        AStub<FixityDeclaration>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported
            get() = when {
                module == null -> false
                module?.exportList == null -> true
                else -> module?.exportList?.childrenStubs
                    ?.filterIsInstance<ExportedOperator.Stub>()
                    ?.find { it.name == name } != null
            }
    }

    override fun getIcon(flags: Int) = AllIcons.Actions.Regex
    override fun getPresentation(): ItemPresentation {
        return object : ItemPresentation {
            override fun getPresentableText() = name
            override fun getLocationString() = module?.name
            override fun getIcon(unused: Boolean) = getIcon(0)
        }
    }

    object Type : WithPsiAndStub<Stub, FixityDeclaration>("FixityDeclaration") {
        override fun createPsi(node: ASTNode) = FixityDeclaration(node)
        override fun createPsi(stub: Stub) = FixityDeclaration(stub, this)
        override fun createStub(psi: FixityDeclaration, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ExportedFixityNameIndex.KEY, stub.name)
                sink.occurrence(ImportableIndex.KEY, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    override val docComments: List<PsiComment>
        get() = getDocComments().ifEmpty {
            (reference.resolve() as? DocCommentOwner)?.docComments ?: emptyList()
        }

    // Todo clean this up
    override fun toString(): String = "PSFixityDeclaration($elementType)"
    override fun asImport() = module?.name?.let { ImportDeclaration(it, false, setOf(ImportedOperator(name))) }
    override val type: PSType? get() = when(val ref = reference.resolve()) {
        is ValueDeclarationGroup -> ref.signature?.type
        is PSClassMember -> ref.type
        else -> null
    }
    val fixity: PSFixity get() = child<PSFixity>()!!
    val associativity get() = fixity.associativity
    val precedence get() = fixity.precedence
    private val isType get(): Boolean = findChildByType<PsiElement>(TYPE) != null
    private val operatorName get() = findNotNullChildByClass(PSOperatorName::class.java)
    val qualifiedIdentifier get() = findChildByClass(PSQualifiedIdentifier::class.java)
    val qualifiedProperName get() = findChildByClass(PSQualifiedProperName::class.java)
    override fun getTextOffset(): Int = nameIdentifier.textOffset
    override fun getNameIdentifier() = operatorName
    override fun getName() = greenStub?.name ?: operatorName.name
    override fun setName(name: String): PsiElement? {
        val identifier =
            project.service<PSPsiFactory>().createOperatorName(name)
                ?: return null
        nameIdentifier.replace(identifier)
        return this
    }

    override fun getReference(): PsiReferenceBase<FixityDeclaration> {
        if (qualifiedIdentifier != null)
            return FixityReference(this)
        else if (isType)
            return TypeFixityReference(this)
        else
            return ConstructorFixityReference(this)
    }
}