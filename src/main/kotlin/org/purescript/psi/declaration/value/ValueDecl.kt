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
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.binder.PSBinderAtom
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.expression.PSExpressionWhere
import org.purescript.psi.expression.PSValue
import org.purescript.psi.name.PSIdentifier
import javax.swing.Icon

interface ValueDecl {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ValueDeclaration") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) =
            Stub(psi.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
    }

    class Psi : PSStubbedElement<Stub>,
        PsiNameIdentifierOwner, DocCommentOwner, Importable {
        constructor(node: ASTNode) : super(node)
        constructor(stub: Stub, type: IStubElementType<*, *>) :
            super(stub, type)

        // Todo clean this up
        override fun toString(): String = "PSValueDeclaration($elementType)"
        override fun asImport(): ImportDeclaration? =
            module?.name?.let { moduleName ->
                ImportDeclaration(moduleName, false, setOf(ImportedValue(name)))
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
                is ValueDecl.Psi ->
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
}
