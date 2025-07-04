package org.purescript.module.declaration.fixity

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.components.service
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentsOfType
import org.purescript.features.DocCommentOwner
import org.purescript.file.PSFile
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedOperator
import org.purescript.inference.inferType
import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.classes.PSClassMember
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.exports.ExportedOperator
import org.purescript.name.PSOperatorName
import org.purescript.name.PSQualifiedIdentifier
import org.purescript.name.PSQualifiedProperName
import org.purescript.psi.AStub
import org.purescript.psi.InlinableElement
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement

class ValueFixityDeclaration : PSStubbedElement<ValueFixityDeclaration.Stub>,
    FixityDeclaration,
    InlinableElement,
    DocCommentOwner {
    class Stub(val name: String, p: StubElement<*>?) :
        AStub<ValueFixityDeclaration>(p, Type) {
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
            override fun getLocationString() = module.name
            override fun getIcon(unused: Boolean) = getIcon(0)
        }
    }

    object Type : WithPsiAndStub<Stub, ValueFixityDeclaration>("FixityDeclaration") {
        override fun createPsi(node: ASTNode) = ValueFixityDeclaration(node)
        override fun createPsi(stub: Stub) = ValueFixityDeclaration(stub, this)
        override fun createStub(psi: ValueFixityDeclaration, p: StubElement<*>?) = Stub(psi.name, p)
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

    override fun canBeInlined(): Boolean = true
    override fun deleteAfterInline() = delete()
    override fun importsAfterInline(): List<ImportDeclaration> {
        val qualifier: String? = this.qualifiedIdentifier?.moduleName?.name
            ?: this.qualifiedProperName?.moduleName?.name
        val importable = reference.resolve() as? Importable
        val import = importable?.asImport()?.withAlias(qualifier)
        return listOfNotNull(import)
    }

    override fun inline(arguments: List<Expression>): Expression {
        val factory = project.service<PSPsiFactory>()
        val name = this.qualifiedIdentifier?.text ?: this.qualifiedProperName?.text
        val (first, second) = arguments.map { it.text }.take(2)
        val expression = "$name $first $second"
        return factory.createParenthesis(expression)?.parentsOfType<Expression>()?.lastOrNull()!!
    }

    override fun unify() =
        unify(reference.inferType((module.containingFile as PSFile).typeSpace.replaceMap()) ?: error("could not find reference for $name"))

    override fun asImport() = module.name.let { ImportDeclaration(it, false, setOf(ImportedOperator(name))) }
    override val type: PSType?
        get() = when (val ref = reference.resolve()) {
            is ValueDeclarationGroup -> ref.signature?.type
            is PSClassMember -> ref.type
            else -> null
        }
    val fixity: PSFixity get() = child()!!
    override val associativity: PSFixity.Associativity get() = fixity.associativity
    override val precedence: Int get() = fixity.precedence
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

    override fun getReference() = FixityReference(this)
}