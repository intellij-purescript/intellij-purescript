package org.purescript.module.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.*
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.name.PSIdentifier
import org.purescript.name.PSModuleName
import org.purescript.name.PSProperName
import org.purescript.name.PSSymbol
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSStubbedElement

sealed class ExportedItem<Stub : AStub<*>> : PSStubbedElement<Stub> {

    constructor(node: ASTNode) : super(node)
    constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)

    abstract override fun getName(): String
    open val constructors: List<PsiNamedElement> = emptyList()
}

interface ExportedData {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type) {
        val dataMembers get() = childrenStubs
            .filterIsInstance<PSExportedDataMemberList.Stub>()
            .singleOrNull()
            ?.childrenStubs
            ?.filterIsInstance<PSExportedDataMember.Stub>()
            ?: emptyList()
    }

    object Type : WithPsiAndStub<Stub, Psi>("ExportedData") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }

    class Psi : ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)

        val properName get() = findNotNullChildByClass(PSProperName::class.java)
        val dataMemberList get() = findChildByClass(PSExportedDataMemberList::class.java)
        val exportsAll get() = dataMemberList?.doubleDot != null
        val dataMembers get() = dataMemberList?.dataMembers ?: emptyArray()
        val dataMembersNames get() = dataMembers.map { it.name }
        val newTypeDeclaration get() = reference.resolve() as? NewtypeDecl
        val dataDeclaration get() = reference.resolve() as? DataDeclaration
        override fun getName() = greenStub?.name ?: properName.name
        override fun getReference() = ExportedDataReference(this)
        override val constructors: List<PsiNamedElement> get() = when(val ref = reference.resolve()) {
            is NewtypeDecl -> listOf(ref.newTypeConstructor)
            is DataDeclaration -> ref.dataConstructors.toList()
            else -> listOf()
        }.let { constructors -> 
            if (exportsAll) constructors
            else {
                val nameSet = dataMembersNames.toSet()
                constructors.filter { (it as PsiNamedElement).name in nameSet }
            }
        }
    }

}

interface ExportedClass {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedClass") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }

    class Psi : ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)

        private val properName get() = findNotNullChildByClass(PSProperName::class.java)
        override fun getName(): String = greenStub?.name ?: properName.name
    }
}

interface ExportedOperator {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedOperator") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }

    class Psi : ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        val symbol get() = findNotNullChildByClass(PSSymbol::class.java)
        override fun getName(): String = greenStub?.name ?: symbol.name
        override fun getReference() = ExportedOperatorReference(this)
    }
}

interface ExportedTypeOperator {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedType") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }

    class Psi : ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)

        val symbol get() = findNotNullChildByClass(PSSymbol::class.java)
        override fun getName(): String = greenStub?.name ?: symbol.name
        override fun getReference() = ExportedTypeOperatorReference(this)
    }
}

class ExportedModule : ExportedItem<ExportedModule.Stub> {
    class Stub(val name: String, p: StubElement<*>?) :
        AStub<ExportedModule>(p, Type)

    object Type : WithPsiAndStub<Stub, ExportedModule>("ExportedModule") {
        override fun createPsi(node: ASTNode) = ExportedModule(node)
        override fun createPsi(stub: Stub) = ExportedModule(stub, this)
        override fun createStub(psi: ExportedModule, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)

    val moduleName get() = findNotNullChildByClass(PSModuleName::class.java)
    val importDeclarations: Sequence<Import>
        get() = module
            .cache
            .importsByName
            ?.get(name)
            ?.asSequence()
            ?: sequenceOf()
    override val constructors get() = 
        if (name != module.name) importDeclarations.flatMap { it.importedConstructors }.toList()
        else module.constructors
    override fun getName(): String = greenStub?.name ?: moduleName.name
    override fun getReference() = ExportedModuleReference(this)
}


interface ExportedValue {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedValue") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }

    class Psi : ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)

        val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
        override fun getName() = greenStub?.name ?: identifier.name
        override fun getReference() = ExportedValueReference(this)
    }
}
