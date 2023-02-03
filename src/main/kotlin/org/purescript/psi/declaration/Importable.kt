package org.purescript.psi.declaration

import com.intellij.psi.PsiElement
import org.purescript.ide.formatting.ImportDeclaration

interface Importable: PsiElement {
    fun asImport(): ImportDeclaration?
}