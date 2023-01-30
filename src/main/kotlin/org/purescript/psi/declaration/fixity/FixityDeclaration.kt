package org.purescript.psi.declaration.fixity

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.stubs.*
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedOperator
import org.purescript.parser.TYPE
import org.purescript.psi.Importable
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.exports.ExportedOperator
import org.purescript.psi.exports.ExportedValue
import org.purescript.psi.module.Module
import org.purescript.psi.name.PSOperatorName
import org.purescript.psi.name.PSQualifiedIdentifier
import org.purescript.psi.name.PSQualifiedProperName

class FixityDeclaration : PSStubbedElement<FixityDeclaration.Stub>,
    PsiNameIdentifierOwner, Importable {
    class Stub(val name: String, p: StubElement<*>?) :
        AStub<FixityDeclaration>(p, Type) {
        val module get() = parentStub as? Module.Stub
        val isExported get() = when {
            module == null -> false
            module?.exportList == null -> true
            else -> module?.exportList?.childrenStubs
                ?.filterIsInstance<ExportedOperator.Stub>()
                ?.find { it.name == name } != null
        }
    }

    object Type : WithPsiAndStub<Stub, FixityDeclaration>("FixityDeclaration") {
        override fun createPsi(node: ASTNode) = FixityDeclaration(node)
        override fun createPsi(stub: Stub) = FixityDeclaration(stub, this)
        override fun createStub(psi: FixityDeclaration, p: StubElement<*>?) =
            Stub(psi.name, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            if (stub.isExported) {
                sink.occurrence(ExportedFixityNameIndex.KEY, stub.name)
            }
        }

        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, type: IStubElementType<*, *>) :
        super(stub, type)

    // Todo clean this up
    override fun toString(): String = "PSFixityDeclaration($elementType)"

    override fun asImport() = module?.name?.let {
        ImportDeclaration(it, false, setOf(ImportedOperator(name)))
    }

    private val isType get(): Boolean =
        findChildByType<PsiElement>(TYPE) != null
    private val operatorName
        get() = findNotNullChildByClass(PSOperatorName::class.java)
    val qualifiedIdentifier: PSQualifiedIdentifier?
        get() = findChildByClass(PSQualifiedIdentifier::class.java)
    val qualifiedProperName: PSQualifiedProperName?
        get() = findChildByClass(PSQualifiedProperName::class.java)

    override fun getTextOffset(): Int = nameIdentifier.textOffset
    override fun getNameIdentifier() = operatorName
    override fun getName() = stub?.name ?: operatorName.name
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