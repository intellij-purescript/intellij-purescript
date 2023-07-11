package org.purescript.module.declaration.fixity

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.inference.Inferable
import org.purescript.module.declaration.Importable

interface FixityDeclaration: PsiElement, PsiNameIdentifierOwner, Inferable, Importable {
    val associativity: PSFixity.Associativity
    val precedence: Int
}