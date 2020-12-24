package net.kenro.ji.jin.purescript.psi

import com.intellij.psi.PsiElement
import java.util.*
import kotlin.streams.asSequence

interface ContainsIdentifier : PsiElement {
    val identifiers: Map<String?, PSIdentifierImpl?>
        get() = Arrays
            .stream(this.children)
            .asSequence()
            .map(this::getIdentifierFromPSI)
            .map { it.entries }
            .flatMap { it.asSequence() }
            .map { Pair(it.key, it.value) }
            .toMap()
    private fun getIdentifierFromPSI(psi: PsiElement): Map<String?, PSIdentifierImpl?> {
        return if (psi is ContainsIdentifier) {
            psi.identifiers
        } else {
            mapOf()
        }
    }
}