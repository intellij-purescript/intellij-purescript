package org.purescript.psi

import com.intellij.psi.PsiElement
import java.util.*
import kotlin.streams.asSequence

interface DeclaresIdentifiers : PsiElement {

    fun getDeclaredIdentifiers() = Arrays
        .stream(this.children)
        .asSequence()
        .map { getIdentifierFromPSI(it) }
        .map { it.entries }
        .flatMap { it.asSequence() }
        .map { Pair(it.key, it.value) }
        .toMap()
    private fun getIdentifierFromPSI(psi: PsiElement): Map<String?, PSIdentifierImpl?> {
        return if (psi is DeclaresIdentifiers) {
            psi.getDeclaredIdentifiers()
        } else {
            mapOf()
        }
    }
}