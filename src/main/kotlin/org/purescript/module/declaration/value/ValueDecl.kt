package org.purescript.module.declaration.value

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.components.service
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.stubs.*
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.prevLeaf
import com.intellij.refactoring.suggested.startOffset
import org.purescript.features.DocCommentOwner
import org.purescript.inference.InferType.Companion.function
import org.purescript.inference.Inferable
import org.purescript.inference.Unifiable
import org.purescript.inference.inferType
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.ExpressionAtom
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.module.declaration.value.parameters.Parameters
import org.purescript.name.PSIdentifier
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement
import javax.swing.Icon

class ValueDecl : PSStubbedElement<ValueDecl.Stub>,
    DocCommentOwner,
    ValueOwner,
    Inferable,
    Unifiable {
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

    val expressions: Sequence<Expression> get() = value?.expressions ?: emptySequence()
    val value get() = findChildByClass(Expression::class.java)
    val expressionAtoms: List<ExpressionAtom>
        get() =
            (value?.expressionAtoms?.toList() ?: emptyList()) +
                    (where?.expressionAtoms ?: emptyList())

    fun setName(name: String): PsiElement? {
        val factory = project.service<PSPsiFactory>()
        val identifier = factory.createIdentifier(name) ?: return null
        nameIdentifier.replace(identifier)
        return this
    }

    override fun getName() = nameIdentifier.name
    override fun addTypeDeclaration(variable: ValueDeclarationGroup): ValueDeclarationGroup {
        val factory = project.service<PSPsiFactory>()
        return when (val w = where) {
            null -> {
                val thisIndent = prevLeaf { it is PsiWhiteSpace } ?: factory.createNewLine()
                val indentText = (thisIndent.text + "  ")
                val indent = project.service<PsiParserFacade>().createWhiteSpaceFromText(indentText)
                add(indent)
                val newWhere =
                    factory.createWhere(indentText.replace("\n", ""), variable) ?: error("could not create where")
                add(newWhere).childrenOfType<ValueDeclarationGroup>().single()
            }

            else -> {
                val thisIndent = w
                    .valueNames.maxBy { it.startOffset }
                    .prevLeaf { it is PsiWhiteSpace } ?: factory.createNewLine()
                val indent = project.service<PsiParserFacade>().createWhiteSpaceFromText(thisIndent.text)
                w.add(indent)
                w.add(variable) as ValueDeclarationGroup
            }
        }
    }

    override val valueNames
        get() =
            parameterValueNames +
                    (where?.valueNames ?: emptySequence())

    override fun getTextOffset(): Int = nameIdentifier.textOffset
    val signature: Signature? get() = doSignature(this.prevSibling)
    private fun doSignature(sibling: PsiElement?): Signature? =
        when (sibling) {
            is PsiWhiteSpace, is PsiComment -> doSignature(sibling.prevSibling)
            is ValueDecl ->
                if (sibling.name == name) sibling.signature
                else null

            is Signature -> sibling
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
    val parameterValueNames get() = parameterList.valueNames
    val namedParameters get() = parameterList.varBinderParameters
    val parameterList get() = findNotNullChildByClass(Parameters::class.java)
    val parameters get() = parameterList.parameters
    val where: PSExpressionWhere? get() = findChildByClass(PSExpressionWhere::class.java)
    fun inline(arguments: List<Expression>): Expression {
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
            factory.createLambda("\\${parametersLeft.joinToString(" ") { it.name }} -> ${copy.value!!.text}")
                ?: error("could not create a lambda from declaration body")
        } else (copy.value ?: error("Copy of value declaration have no value node"))
    }

    fun canBeInlined(): Boolean {
        val binders = parameterList.parameterBinders
        return !binders.any { it !is VarBinder }
    }
    override fun unify() {
        unify(parameters.foldRight(value?.inferType() ?: return) { parameter, ret -> 
                function(parameter.inferType(), ret) 
            })
    }
}