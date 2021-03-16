package org.purescript.psi.classes

import com.intellij.lang.ASTNode
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSTypeVarImpl

/**
 * The members part of a class declaration, e.g.
 * ```
 * decodeJson :: Json -> Either JsonDecodeError a
 * ```
 * in
 * ```
 * class DecodeJson a where
 *   decodeJson :: Json -> Either JsonDecodeError a
 * ```
 */
class PSClassMember(node: ASTNode) : PSPsiElement(node) {
}
