package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeImpl

/**
 * The a member function of a class declaration, e.g.
 * ```
 * decodeJson :: Json -> Either JsonDecodeError a
 * ```
 * in
 * ```
 * class DecodeJson a where
 *   decodeJson :: Json -> Either JsonDecodeError a
 * ```
 */
class PSClassMember(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner {

    /**
     * @return the [PSIdentifier] identifying this member, e.g.
     * ```
     * decodeJson
     * ```
     * in
     * ```
     * decodeJson :: Json -> Either JsonDecodeError a
     * ```
     */
    val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)

    /**
     * @return the [PSTypeImpl] specifying this member's type signature, e.g.
     * ```
     * Json -> Either JsonDecodeError a
     * ```
     * in
     * ```
     * decodeJson :: Json -> Either JsonDecodeError a
     * ```
     */
    val type get() = findNotNullChildByClass(PSTypeImpl::class.java)

    override fun setName(name: String): PsiElement? = null

    override fun getNameIdentifier(): PsiElement = identifier

    override fun getName(): String = identifier.name
}
