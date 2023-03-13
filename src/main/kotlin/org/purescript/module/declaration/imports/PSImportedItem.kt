package org.purescript.module.declaration.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.ide.formatting.*
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.name.PSIdentifier
import org.purescript.name.PSProperName
import org.purescript.name.PSSymbol
import org.purescript.psi.PSPsiElement

/**
 * Any element that can occur in a [PSImportList]
 */
sealed class PSImportedItem(node: ASTNode) : PSPsiElement(node), Comparable<PSImportedItem> {
    abstract override fun getName(): String
    abstract fun nameMatches(name: String): Boolean
    internal val importDeclaration: Import
        get() =
            PsiTreeUtil.getParentOfType(this, Import::class.java)!!

    abstract fun asData(): ImportedItem

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
            { it.name }
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
class PSImportedClass(node: ASTNode) : PSImportedItem(node) {
    internal val properName: PSProperName get() = findNotNullChildByClass(PSProperName::class.java)
    override fun getName(): String = properName.name
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
class PSImportedData(node: ASTNode) : PSImportedItem(node) {
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
    override fun getName(): String = properName.name
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
class PSImportedOperator(node: ASTNode) : PSImportedItem(node) {
    val symbol: PSSymbol get() = findNotNullChildByClass(PSSymbol::class.java)
    override fun getName(): String = symbol.name
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
class PSImportedType(node: ASTNode) : PSImportedItem(node) {
    private val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName(): String = identifier.name
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
class PSImportedValue(node: ASTNode) : PSImportedItem(node) {
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName(): String = identifier.name
    override fun nameMatches(name: String): Boolean = identifier.nameMatches(name)
    override fun asData() = ImportedValue(name)
    override fun getReference(): ImportedValueReference = ImportedValueReference(this)
}
