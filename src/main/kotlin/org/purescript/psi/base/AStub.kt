package org.purescript.psi.base

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement

abstract class AStub<Psi: PsiElement>(parent: StubElement<*>?, type: IStubElementType<*, Psi>):
    StubBase<Psi>(parent, type), StubElement<Psi>