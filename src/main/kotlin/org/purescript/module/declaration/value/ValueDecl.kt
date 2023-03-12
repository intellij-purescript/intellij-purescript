package org.purescript.module.declaration.value

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.components.service
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.module.declaration.signature.PSSignature
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.PSValue
import org.purescript.module.declaration.value.expression.identifier.Argument
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.module.declaration.value.parameters.Parameters
import org.purescript.name.PSIdentifier
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement
import javax.swing.Icon

class ValueDecl : PSStubbedElement<ValueDecl.Stub>, DocCommentOwner, ValueNamespace {
    class Stub(val name: String, p: StubElement<*>?) : AStub<ValueDecl>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, ValueDecl>("ValueDecl") {
        override fun createPsi(node: ASTNode) = ValueDecl(node)
        override fun createPsi(stub: Stub) = ValueDecl(stub, this)
        override fun createStub(valueDecl: ValueDecl, p: StubElement<*>?) = Stub(valueDecl.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) {}
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    val expressions: Sequence<Expression> get() = value.expressions
    val value get() = findChildByClass(PSValue::class.java)!!
    val expressionAtoms: List<ExpressionAtom>
        get() =
            value.expressionAtoms.toList() +
                    (where?.expressionAtoms ?: emptyList())

    fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val identifier = factory.createIdentifier(name) ?: return null
        nameIdentifier.replace(identifier)
        return this
    }
    override fun getName() = nameIdentifier.name
    override val valueNames get() = 
        parameterValueNames + 
                (where?.valueNames ?: emptySequence())
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
        val parameterList = parameters.map { " " + it.text.trim() }.joinToString("")
        val type = signature?.text?.substringAfter(name) ?: ""
        val presentableText = "$name$parameterList$type".replace(Regex("\\s+"), " ")
        val fileName = this.containingFile.name
        return object : ItemPresentation {
            override fun getPresentableText(): String = presentableText
            override fun getLocationString(): String = fileName
            override fun getIcon(unused: Boolean): Icon? = null
        }
    }

    val nameIdentifier: PSIdentifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override val docComments get() = this.getDocComments()
    val parameterValueNames get() = parameterList?.valueNames ?: emptySequence()
    val namedParameters get() = parameterList?.varBinderParameters ?: emptyList()
    val parameterList get() = findChildByClass(Parameters::class.java)
    val parameters get() = parameterList?.parameters ?: emptyList()
    val where: PSExpressionWhere? get() = findChildByClass(PSExpressionWhere::class.java)
    val valueDeclarationGroups get() = where?.valueDeclarationGroups ?: emptyArray()
    fun inline(arguments: List<Argument>): Expression {
        val copy = this.copy() as ValueDecl
        val binders = copy.namedParameters
        val parametersToInline = binders.map {
            ReferencesSearch
                .search(it, LocalSearchScope(copy))
                .findAll()
                .map { it.element }
        }.toList()
        arguments.zip(parametersToInline) { argument, toInline ->
            for (place in toInline) {
                place.replace(argument.firstChild)
            }
        }
        return if (arguments.size < parametersToInline.size) {
            val parametersLeft = binders.drop(arguments.size)
            val factory = project.service<PSPsiFactory>()
            factory.createLambda("\\${parametersLeft.joinToString(" ") { it.name }} -> ${copy.value.text}") ?:
                error("could not create a lambda from declaration body")
        } else copy.value
    }
}