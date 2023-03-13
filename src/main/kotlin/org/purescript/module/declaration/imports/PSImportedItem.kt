package org.purescript.module.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.*
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.ide.formatting.*
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.name.PSIdentifier
import org.purescript.name.PSProperName
import org.purescript.name.PSSymbol
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSStubbedElement

/**
 * Any element that can occur in a [PSImportList]
 */
sealed interface PSImportedItem : PsiElement, Comparable<PSImportedItem> {
    fun getName(): String
    fun nameMatches(name: String): Boolean
    val importDeclaration: Import get() = PsiTreeUtil.getParentOfType(this, Import::class.java)!!
    fun asData(): ImportedItem

    /**
     * Compares this [PSImportedItem] with the specified [PSImportedItem] for order.
     * Returns zero if this [PSImportedItem] is equal to the specified
     * [other] [PSImportedItem], a negative number if it's less than [other], or a
     * positive number if it's greater than [other].
     *
     * Different classes of [PSImportedItem] are ordered accordingly, in ascending order:
     *  - [PSImportedClass]
     *  - [PSImportedType]
     *  - [PSImportedData]
     *  - [PSImportedValue]
     *  - [PSImportedOperator]
     *
     * If the operands are of the same class, they are ordered according to their [getName].
     */
    override fun compareTo(other: PSImportedItem): Int =
        compareValuesBy(
            this,
            other,
            {
                when (it) {
                    is PSImportedClass -> 0
                    is PSImportedType -> 2
                    is PSImportedData -> 3
                    is PSImportedValue -> 4
                    is PSImportedOperator -> 5
                }
            },
            { it.getName() }
        )
}

/**
 * An imported class declaration, e.g.
 * ```
 * class Newtype
 * ```
 * in
 * ```
 * import Data.Newtype (class Newtype)
 * ```
 */
class PSImportedClass : PSStubbedElement<PSImportedClass.Stub>, PSImportedItem {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSImportedClass>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSImportedClass>("ImportedClass") {
        override fun createPsi(node: ASTNode) = PSImportedClass(node)
        override fun createPsi(stub: Stub) = PSImportedClass(stub, this)
        override fun createStub(my: PSImportedClass, p: StubElement<*>?) = Stub(my.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    internal val properName: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)
    override fun getName(): String = greenStub?.name ?: properName.name
    override fun nameMatches(name: String): Boolean = properName.nameMatches(name)
    override fun asData() = ImportedClass(name)
    override fun getReference(): ImportedClassReference = ImportedClassReference(this)
}

/**
 * An imported data, type, or newtype declaration, e.g.
 * ```
 * Maybe(..)
 * ```
 * in
 * ```
 * import Data.Maybe (Maybe(..))
 * ```
 */
class PSImportedData : PSStubbedElement<PSImportedData.Stub>, PSImportedItem {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSImportedData>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSImportedData>("ImportedData") {
        override fun createPsi(node: ASTNode) = PSImportedData(node)
        override fun createPsi(stub: Stub) = PSImportedData(stub, this)
        override fun createStub(my: PSImportedData, p: StubElement<*>?) = Stub(my.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    /**
     * @return the [PSProperName] identifying this element
     */
    internal val properName get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSImportedDataMemberList] containing the imported members,
     * if present
     */
    internal val importedDataMemberList get() = findChildByClass(PSImportedDataMemberList::class.java)

    /**
     * @return true if this element implicitly imports all members using
     * the (..) syntax, otherwise false
     */
    val importsAll get() = importedDataMemberList?.doubleDot != null

    /**
     * @return the data members imported explicitly using the
     * Type(A, B, C) syntax
     */
    val importedDataMembers get() = importedDataMemberList?.dataMembers ?: emptyArray()

    /**
     * @return the [NewtypeDecl] that this element references to,
     * if it exists
     */
    val newTypeDeclaration get() = reference.resolve() as? NewtypeDecl

    /**
     * @return the [DataDeclaration] that this element references to,
     * if it exists
     */
    val dataDeclaration get() = reference.resolve() as? DataDeclaration
    override fun getName(): String = greenStub?.name ?: properName.name
    override fun nameMatches(name: String): Boolean = properName.nameMatches(name)
    override fun asData() = ImportedData(name, importsAll, importedDataMembers.map { it.name }.toSet())
    override fun getReference() = ImportedDataReference(this)
}

/**
 * An imported infix operator declaration, e.g.
 * ```
 * (==)
 * ```
 * in
 * ```
 * import Data.Eq ((==))
 * ```
 */
class PSImportedOperator : PSStubbedElement<PSImportedOperator.Stub>, PSImportedItem {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSImportedOperator>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSImportedOperator>("ImportedOperator") {
        override fun createPsi(node: ASTNode) = PSImportedOperator(node)
        override fun createPsi(stub: Stub) = PSImportedOperator(stub, this)
        override fun createStub(my: PSImportedOperator, p: StubElement<*>?) = Stub(my.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    val symbol: PSSymbol get() = findNotNullChildByClass(PSSymbol::class.java)
    override fun getName(): String = greenStub?.name ?: symbol.name
    override fun nameMatches(name: String) = symbol.nameMatches(name)
    override fun asData() = ImportedOperator(name)
    override fun getReference() = ImportedOperatorReference(this)
}

/**
 * An imported infix type operator declaration, e.g.
 * ```
 * type (~>)
 * ```
 * in
 * ```
 * import Prelude (type (~>))
 * ```
 */
class PSImportedType : PSStubbedElement<PSImportedType.Stub>, PSImportedItem {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSImportedType>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSImportedType>("ImportedType") {
        override fun createPsi(node: ASTNode) = PSImportedType(node)
        override fun createPsi(stub: Stub) = PSImportedType(stub, this)
        override fun createStub(my: PSImportedType, p: StubElement<*>?) = Stub(my.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    private val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName(): String = greenStub?.name ?: identifier.name
    override fun nameMatches(name: String) = identifier.nameMatches(name)
    override fun asData() = ImportedType(name)
}

/**
 * An imported value or class member declaration, e.g.
 * ```
 * show
 * ```
 * in
 * ```
 * import Prelude (show)
 * ```
 */
class PSImportedValue : PSStubbedElement<PSImportedValue.Stub>, PSImportedItem {
    class Stub(val name: String, p: StubElement<*>?) : AStub<PSImportedValue>(p, Type)
    object Type : PSElementType.WithPsiAndStub<Stub, PSImportedValue>("ImportedValue") {
        override fun createPsi(node: ASTNode) = PSImportedValue(node)
        override fun createPsi(stub: Stub) = PSImportedValue(stub, this)
        override fun createStub(my: PSImportedValue, p: StubElement<*>?) = Stub(my.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) = d.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub = Stub(d.readNameString()!!, p)
    }

    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)
    
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName(): String = greenStub?.name ?: identifier.name
    override fun nameMatches(name: String): Boolean = identifier.nameMatches(name)
    override fun asData() = ImportedValue(name)
    override fun getReference(): ImportedValueReference = ImportedValueReference(this)
}
