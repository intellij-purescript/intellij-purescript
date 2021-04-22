package org.purescript.psi

import com.intellij.lang.ASTNode

/**
 * `foo :: int` in
 * ```
 * foo :: Int
 * foo = 42
 * ```
 */
class PSSignature(node: ASTNode) : PSPsiElement(node)