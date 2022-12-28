package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import com.intellij.psi.stubs.*
import com.intellij.psi.util.parentOfType
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType.WithPsiAndStub
import org.purescript.psi.PSStubbedElement
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.imports.PSImportDeclaration
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSProperName
import org.purescript.psi.name.PSSymbol
import org.purescript.psi.module.Module
import org.purescript.psi.newtype.PSNewTypeDeclaration

sealed class ExportedItem<Stub : AStub<*>> : PSStubbedElement<Stub> {
    constructor(node: ASTNode) : super(node)
    constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
    abstract override fun getName(): String
}

interface ExportedData {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
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
        // Todo clean this up
        override fun toString(): String = "PSExportedData($elementType)"
        val properName get() = findNotNullChildByClass(PSProperName::class.java)
        val dataMemberList get() = findChildByClass(PSExportedDataMemberList::class.java)
        val exportsAll get() = dataMemberList?.doubleDot != null
        val dataMembers get() = dataMemberList?.dataMembers ?: emptyArray()
        val newTypeDeclaration get() = reference.resolve() as? PSNewTypeDeclaration
        val dataDeclaration get() = reference.resolve() as? PSDataDeclaration
        override fun getName() = greenStub?.name ?: properName.name
        override fun getReference() = ExportedDataReference(this)
    }

}

interface ExportedClass {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedClass") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
    }
    class Psi: ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportedClass($elementType)"
        private val properName get() = findNotNullChildByClass(PSProperName::class.java)
        override fun getName(): String = greenStub?.name ?: properName.name
    }
}

interface ExportedKind {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedKind") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)
    }
    class Psi: ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportedKind($elementType)"
        private val properName get() = findNotNullChildByClass(PSProperName::class.java)
        override fun getName(): String = properName.name
    }

}

interface ExportedOperator {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedOperator") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)
    }
    class Psi: ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportedOperator($elementType)"
        val symbol get() = findNotNullChildByClass(PSSymbol::class.java)
        override fun getName(): String = greenStub?.name ?: symbol.name
        override fun getReference() = ExportedOperatorReference(this)
    }
}

interface ExportedType {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedType") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)
    }
    class Psi: ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportedType($elementType)"
        private val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
        override fun getName(): String = greenStub?.name ?: identifier.name
    }
}

interface ExportedModule {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedModule") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)
    }
    class Psi: ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportedModule($elementType)"
        val moduleName get() = findNotNullChildByClass(PSModuleName::class.java)
        val importDeclarations: Sequence<PSImportDeclaration>
            get() = module
                ?.cache
                ?.importsByName
                ?.get(name)
                ?.asSequence()
                ?: sequenceOf()
        override fun getName(): String = greenStub?.name ?: moduleName.name
        override fun getReference() = ExportedModuleReference(this)
    }

}

interface ExportedValue {
    class Stub(val name:String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ExportedValue") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>?) = Stub(psi.name, p)
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)
    }
    class Psi: ExportedItem<Stub> {
        constructor(node: ASTNode) : super(node)
        constructor(s: Stub, t: IStubElementType<*, *>) : super(s, t)
        // Todo clean this up
        override fun toString(): String = "PSExportedValue($elementType)"
        val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
        override fun getName() = greenStub?.name ?: identifier.name
        override fun getReference() = ExportedValueReference(this)
    }
}
