package org.purescript.psi.declaration

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.psi.declaration.signature.PSSignature

interface Importable: PsiNamedElement {
    fun asImport(): ImportDeclaration?
    val signature: PSSignature?
}