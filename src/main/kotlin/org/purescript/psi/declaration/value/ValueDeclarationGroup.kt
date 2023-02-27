package org.purescript.psi.declaration.value

import com.intellij.icons.AllIcons
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.*
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedValue
import org.purescript.psi.PSElementType
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.binder.PSBinderAtom
import org.purescript.psi.declaration.Importable
import org.purescript.psi.declaration.ImportableIndex
import org.purescript.psi.declaration.signature.PSSignature
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.expression.ExpressionAtom
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.type.PSType

class ValueDeclarationGroup: PSStubbedElement<ValueDeclarationGroup.Stub>,
    PsiNameIdentifierOwner, DocCommentOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) : AStub<ValueDeclarationGroup>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isTopLevel get() = module != null
        val isExported get() = when {
            module == null -> false
            module?.exportList == null -> true
            else -> module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedValue.Stub>()
                ?.find { it.name == name } != null
        }
    }

    object Type : PSElementType.WithPsiAndStub<Stub, ValueDeclarationGroup>("ValueDeclarationGroup") {
        override fun createPsi(node: ASTNode) = ValueDeclarationGroup(node)
        override fun createPsi(stub: Stub) = ValueDeclarationGroup(stub, this)
        override fun createStub(me: ValueDeclarationGroup, p: StubElement<*>?) =
            Stub(me.name, p)

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)

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

    override fun getIcon(flags: Int) = AllIcons.Nodes.Function
    override fun getPresentation() = object : ItemPresentation {
        override fun getPresentableText() = name
        override fun getLocationString() = module?.name
        override fun getIcon(unused: Boolean) = getIcon(0)
    }
    val signature: PSSignature? get() = findChildByClass(PSSignature::class.java)
    val valueDeclarations: Array<out ValueDecl> get() = children()
    val expressionAtoms get() = valueDeclarations.flatMap { it.expressionAtoms }
    val binderAtoms get() = sequence {
        var steps = children.asList()
        while (steps.isNotEmpty()) {
            this.yieldAll(steps)
            steps = steps.flatMap { it.children.asList() }
        }
    }.filterIsInstance<PSBinderAtom>().toList()
    
    override fun setName(name: String): PsiElement {
        for (valueDeclaration in valueDeclarations) {
            valueDeclaration.setName(name)
        }
        signature?.setName(name)
        return this
    }

    override fun getName(): String {
        return nameIdentifier.name
    }

    override fun getNameIdentifier(): PSIdentifier =
        valueDeclarations.first().nameIdentifier

    override fun getTextOffset(): Int {
        return nameIdentifier.textOffset
    }

    override val docComments: List<PsiComment>
        get() = this.getDocComments() + valueDeclarations.flatMap { it.docComments }.toList()

    override fun asImport() = module?.name?.let {
        ImportDeclaration(it, false, setOf(ImportedValue(name)))
    }

    override val type: PSType? get() = signature?.type
}