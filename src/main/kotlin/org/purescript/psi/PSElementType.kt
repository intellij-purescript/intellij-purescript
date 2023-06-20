package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.purescript.PSLanguage

open class PSElementType(@NonNls debugName: String) :
    IElementType(debugName, PSLanguage) {
    interface HasPsi<Psi: PsiElement> {
        fun createPsi(node: ASTNode): Psi
    }
    open class WithPsi<Psi: PsiElement>(
        @NonNls debugName: String,
        val constructor: (ASTNode) -> Psi
    ) : HasPsi<Psi>, PSElementType(debugName) {
        override fun createPsi(node: ASTNode): Psi = constructor(node)
    }

    abstract class WithPsiAndStub<S : StubElement<*>?, E : PsiElement>(
        @NonNls debugName: String
    ) : HasPsi<E>, IStubElementType<S, E>(debugName, PSLanguage) {
        override fun getExternalId(): String {
            return "purescript.$this"
        }
    }
}