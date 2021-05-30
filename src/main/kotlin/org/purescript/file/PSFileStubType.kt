package org.purescript.file

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import org.purescript.PSLanguage

class PSFileStubType : IStubFileElementType<PsiFileStub<PSFile>?>(org.purescript.PSLanguage.INSTANCE) {
    companion object {
        val INSTANCE: PSFileStubType = PSFileStubType()
    }
}