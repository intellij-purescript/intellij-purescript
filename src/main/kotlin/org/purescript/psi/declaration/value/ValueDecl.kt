package org.purescript.psi.declaration.value

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.components.service
import com.intellij.psi.*
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.Importable
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.binder.PSBinderAtom
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.expression.PSExpressionWhere
import org.purescript.psi.expression.PSValue
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.module.Module
import javax.swing.Icon

class ValueDecl : PSStubbedElement<ValueDecl.Stub>,
    PsiNameIdentifierOwner, DocCommentOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<ValueDecl>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported get() = when {
            module == null -> false
            module?.exportList == null -> true
            else -> module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedValue.Stub>()
                ?.find { it.name == name } != null
        }
    }
    
    object Type : PSElementType.WithPsiAndStub<Stub, ValueDecl>("ValueDecl") {
        override fun createPsi(node: ASTNode) = ValueDecl(node)
        override fun createPsi(stub: Stub) = ValueDecl(stub, this)
        override fun createStub(valueDecl: ValueDecl, p: StubElement<*>?) =
            Stub(valueDecl.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ExportedValueDeclNameIndex.KEY, stub.name)
            }
        }
    }
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) :
        super(stub, type)

    override fun asImport() = module?.name?.let {
        ImportDeclaration(it, false, setOf(ImportedValue(name)))
    }

    val value get() = findChildByClass(PSValue::class.java)!!
    val expressionAtoms: List<ExpressionAtom>
        get() =
            value.expressionAtoms.toList() +
                (where?.expressionAtoms ?: emptyList())

    override fun getName(): String {
        return findChildByClass(PSIdentifier::class.java)!!
            .name
    }

    override fun setName(name: String): PsiElement? {
        val identifier =
            project.service<PSPsiFactory>().createIdentifier(name)
                ?: return null
        nameIdentifier.replace(identifier)
        return this
    }

    override fun getTextOffset(): Int = nameIdentifier.textOffset

    val signature: PSSignature? get() = doSignature(this.prevSibling)
    private fun doSignature(sibling: PsiElement?): PSSignature? =
        when (sibling) {
            is PsiWhiteSpace, is PsiComment -> doSignature(sibling.prevSibling)
            is ValueDecl ->
                if (sibling.name == name) sibling.signature
                else null

            is PSSignature -> sibling
            else -> null
        }

    override fun getPresentation(): ItemPresentation {
        val name = this.name
        val parameters = findChildrenByClass(PSBinderAtom::class.java)
        val parameterList = parameters
            .asSequence()
            .map { " " + it.text.trim() }
            .joinToString("")
        val type = signature?.text?.substringAfter(name) ?: ""
        val presentableText =
            "$name$parameterList$type".replace(Regex("\\s+"), " ")
        val fileName = this.containingFile.name
        return object : ItemPresentation {
            override fun getPresentableText(): String {
                return presentableText
            }

            override fun getLocationString(): String {
                return fileName
            }

            override fun getIcon(unused: Boolean): Icon? {
                return null
            }
        }
    }

    override fun getNameIdentifier(): PSIdentifier {
        return findNotNullChildByClass(PSIdentifier::class.java)
    }

    override fun getReference(): PsiReference? {
        val valueDeclarationSelfReference =
            ValueDeclSelfReference(this)
        return if (valueDeclarationSelfReference.resolve() == this) {
            null
        } else {
            valueDeclarationSelfReference
        }
    }

    override val docComments: List<PsiComment>
        get() = this.getDocComments()

    val varBindersInParameters: Map<String, PSVarBinder>
        get() = SyntaxTraverser.psiTraverser(this)
            .filterIsInstance(PSVarBinder::class.java)
            .asSequence()
            .map { Pair(it.name, it) }
            .toMap()

    val where: PSExpressionWhere? get() = findChildByClass(PSExpressionWhere::class.java)

}