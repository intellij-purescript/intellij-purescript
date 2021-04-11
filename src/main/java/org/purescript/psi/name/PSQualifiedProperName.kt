package org.purescript.psi.name

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement

/**
 * A qualified proper name, e.g.
 * ```
 * Data.Maybe.Nothing
 * ```
 * in
 * ```
 * import Data.Maybe as Data.Maybe
 * f = Data.Maybe.Nothing
 */
class PSQualifiedProperName(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the module prefix of this element, including the dot, e.g.
     * ```
     * Data.Maybe.
     * ```
     * in
     * ```
     * Data.Maybe.Nothing
     * ```
     */
    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)


    /**
     * @return the proper name part of this element, e.g.
     * ```
     * Nothing
     * ```
     * in
     * ```
     * Data.Maybe.Nothing
     * ```
     */
    val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    /**
     * @return the name of the [PSProperName] that this element contains
     */
    override fun getName(): String = properName.name
}
