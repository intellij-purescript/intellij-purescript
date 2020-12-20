package net.kenro.ji.jin.purescript.psi

import com.intellij.psi.PsiElement
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl
import kotlin.collections.MutableMap.MutableEntry
import java.util.stream.Collectors
import net.kenro.ji.jin.purescript.psi.ContainsIdentifier
import net.kenro.ji.jin.purescript.psi.DeclaresIdentifiers
import net.kenro.ji.jin.purescript.psi.PSElementType
import org.jetbrains.annotations.NonNls
import com.intellij.psi.tree.IElementType
import net.kenro.ji.jin.purescript.PSLanguage
import com.intellij.psi.tree.TokenSet
import net.kenro.ji.jin.purescript.psi.PSTokens
import java.util.*
import java.util.function.Function
import java.util.stream.Stream
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