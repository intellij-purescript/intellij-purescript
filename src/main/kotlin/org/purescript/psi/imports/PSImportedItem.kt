package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.psi.base.PSPsiElement
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.name.PSProperName
import org.purescript.psi.name.PSSymbol
import org.purescript.psi.newtype.PSNewTypeDeclaration

/**
 * Any element that can occur in a [PSImportList]
 */
sealed class PSImportedItem(node: ASTNode) : PSPsiElement(node), Comparable<PSImportedItem> {
    abstract override fun getName(): String

    internal val importDeclaration: Import.Psi?
        get() =
            PsiTreeUtil.getParentOfType(this, Import.Psi::class.java)

    /**
     * Compares this [PSImportedItem] with the specified [PSImportedItem] for order.
     * Returns zero if this [PSImportedItem] is equal to the specified
     * [other] [PSImportedItem], a negative number if it's less than [other], or a
     * positive number if it's greater than [other].
     *
     * Different classes of [PSImportedItem] are ordered accordingly, in ascending order:
     *  - [PSImportedClass]
     *  - [PSImportedKind]
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
                    is PSImportedKind -> 1
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
    internal val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name

    override fun getReference(): ImportedClassReference =
        ImportedClassReference(this)
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
    internal val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the [PSImportedDataMemberList] containing the imported members,
     * if present
     */
    internal val importedDataMemberList: PSImportedDataMemberList?
        get() = findChildByClass(PSImportedDataMemberList::class.java)

    /**
     * @return true if this element implicitly imports all members using
     * the (..) syntax, otherwise false
     */
    val importsAll: Boolean
        get() = importedDataMemberList?.doubleDot != null

    /**
     * @return the data members imported explicitly using the
     * Type(A, B, C) syntax
     */
    val importedDataMembers: Array<PSImportedDataMember>
        get() = importedDataMemberList?.dataMembers ?: emptyArray()

    /**
     * @return the [PSNewTypeDeclaration] that this element references to,
     * if it exists
     */
    val newTypeDeclaration: PSNewTypeDeclaration?
        get() = reference.resolve() as? PSNewTypeDeclaration

    /**
     * @return the [PSDataDeclaration] that this element references to,
     * if it exists
     */
    val dataDeclaration: PSDataDeclaration?
        get() = reference.resolve() as? PSDataDeclaration

    override fun getName(): String = properName.name

    override fun getReference(): ImportedDataReference =
        ImportedDataReference(this)
}

/**
 * An imported kind declaration, e.g.
 * ```
 * kind Boolean
 * ```
 * in
 * ```
 * import Type.Data.Boolean (kind Boolean)
 * ```
 */
class PSImportedKind(node: ASTNode) : PSImportedItem(node) {
    private val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
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
    val symbol: PSSymbol
        get() =
            findNotNullChildByClass(PSSymbol::class.java)

    override fun getName(): String = symbol.name

    override fun getReference(): PsiReference {
        return ImportedOperatorReference(this)
    }
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
    private val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name
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
    val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name

    override fun getReference(): ImportedValueReference =
        ImportedValueReference(this)
}
