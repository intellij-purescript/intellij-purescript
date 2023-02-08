package org.purescript.psi.declaration

import com.intellij.psi.PsiNamedElement
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.psi.type.PSType

interface Importable: PsiNamedElement {
    fun asImport(): ImportDeclaration?
    val type: PSType?
}