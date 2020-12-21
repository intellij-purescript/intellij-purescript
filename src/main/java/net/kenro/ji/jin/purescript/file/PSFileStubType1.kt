package net.kenro.ji.jin.purescript.file

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import net.kenro.ji.jin.purescript.PSLanguage

class PSFileStubType : IStubFileElementType<PsiFileStub<PSFile>?>(PSLanguage.INSTANCE) {
    companion object {
        val INSTANCE: PSFileStubType = PSFileStubType()
    }
}