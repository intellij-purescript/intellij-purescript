package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.psi.PSIdentifier
import org.purescript.psi.name.PSProperName
import org.purescript.psi.PSPsiElement

/**
 * Any element that can occur in a [PSImportList]
 */
sealed class PSImportedItem(node: ASTNode) : PSPsiElement(node) {
    abstract override fun getName(): String

    internal val importDeclaration: PSImportDeclarationImpl?
        get() =
            PsiTreeUtil.getParentOfType(this, PSImportDeclarationImpl::class.java)
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
    internal val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name

    override fun getReference(): ImportedDataReference = ImportedDataReference(this)
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
    private val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name
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
