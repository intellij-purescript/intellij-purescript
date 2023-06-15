package org.purescript.module.declaration.value

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.*
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.util.alsoIfNull
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.module.Module
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.ImportableIndex
import org.purescript.module.declaration.Signature
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.declaration.value.binder.Binder
import org.purescript.module.declaration.value.expression.Expression
import org.purescript.module.declaration.value.expression.dostmt.PSDoNotationLet
import org.purescript.module.declaration.value.expression.namespace.Let
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.name.PSIdentifier
import org.purescript.psi.*
import org.purescript.typechecker.TypeCheckable

class ValueDeclarationGroup : PSStubbedElement<ValueDeclarationGroup.Stub>,
    PsiNameIdentifierOwner, DocCommentOwner, Importable, UsedElement, InlinableElement, TypeCheckable {
    class Stub(val name: String, val isExported: Boolean, p: StubElement<*>?) : AStub<ValueDeclarationGroup>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isTopLevel get() = module != null
    }

    object Type : PSElementType.WithPsiAndStub<Stub, ValueDeclarationGroup>("ValueDeclarationGroup") {
        override fun createPsi(node: ASTNode) = ValueDeclarationGroup(node)
        override fun createPsi(stub: Stub) = ValueDeclarationGroup(stub, this)
        override fun createStub(me: ValueDeclarationGroup, p: StubElement<*>?) = Stub(me.name, me.isExported, p)
        override fun serialize(stub: Stub, d: StubOutputStream) {
            d.writeName(stub.name)
            d.writeBoolean(stub.isExported)
        }

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(
            d.readNameString()!!,
            d.readBoolean(),
            p
        )

        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ImportableIndex.KEY, stub.name)
                sink.occurrence(ExportedValueDecl.KEY, stub.name)
            }
            if (stub.isTopLevel) {
                sink.occurrence(TopLevelValueDecl.KEY, stub.name)
            }
        }
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) : super(stub, type)

    override fun inline(arguments: List<Expression>): Expression {
        val valueDeclaration =
            valueDeclarations.singleOrNull() ?: error("can only inline value declarations with one body")
        return valueDeclaration.inline(arguments)
    }

    override fun canBeInlined(): Boolean =
        valueDeclarations.singleOrNull()
            ?.canBeInlined()
            ?: false

    override fun deleteAfterInline() {
        when (val parent = parent) {
            is Let ->
                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                    parent.value?.let { parent.parent.parent.replace(it) }
                        .alsoIfNull { delete() }
                } else {
                    delete()
                }

            is PSDoNotationLet, is PSExpressionWhere ->
                if (parent.childrenOfType<ValueDeclarationGroup>().size == 1) {
                    parent.delete()
                } else {
                    delete()
                }

            else -> delete()
        }
    }

    override fun importsAfterInline(): List<ImportDeclaration> = emptyList()

    override fun getIcon(flags: Int) = AllIcons.Nodes.Function
    override fun getPresentation() = object : ItemPresentation {
        override fun getPresentableText() = name
        override fun getLocationString() = module?.name
        override fun getIcon(unused: Boolean) = getIcon(0)
    }

    val signature: Signature? get() = child()
    val valueDeclarations: Array<out ValueDecl> get() = children()
    val expressionAtoms get() = valueDeclarations.flatMap { it.expressionAtoms }
    val binderAtoms
        get() = sequence {
            var steps = children.asList()
            while (steps.isNotEmpty()) {
                this.yieldAll(steps)
                steps = steps.flatMap { it.children.asList() }
            }
        }.filterIsInstance<Binder>().toList()

    override fun setName(name: String): PsiElement {
        for (valueDeclaration in valueDeclarations) {
            valueDeclaration.name = name
        }
        signature?.name = name
        return this
    }

    override fun getName(): String = greenStub?.name ?: nameIdentifier.name
    override fun getNameIdentifier(): PSIdentifier = valueDeclarations.first().nameIdentifier
    override fun getTextOffset(): Int = nameIdentifier.textOffset
    override val docComments: List<PsiComment>
        get() = this.getDocComments() + valueDeclarations.flatMap { it.docComments }.toList()

    override fun asImport() = module?.name?.let { ImportDeclaration(it, false, setOf(ImportedValue(name))) }
    override val type: PSType? get() = signature?.type
    val isExported
        get() = greenStub?.isExported ?: when {
            module == null -> false
            !isTopLevel -> false
            module?.exports == null -> true
            module?.exportsSelf == null -> true
            else -> name in (module?.exports?.values?.map { it.name } ?: emptyList())
        }
    val isTopLevel get() = parent is Module
    override fun getUseScope(): SearchScope = (
            if (isExported) super.getUseScope()
            else if (isExported) module?.let { LocalSearchScope(it) }
            else (parentsOfType<ValueDecl>().lastOrNull() ?: module)?.let { LocalSearchScope(it) })
        ?: super.getUseScope()

    override fun checkReferenceType() = signature?.checkType()
    override fun checkUsageType() = 
        valueDeclarations.firstNotNullOfOrNull { it.checkType() }
}