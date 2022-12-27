package org.purescript.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement

abstract class PSStubbedElement<Stub: StubElement<*>>: 
    StubBasedPsiElement<Stub>,
    StubBasedPsiElementBase<Stub> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, nodeType: IStubElementType<*, *>) : super(stub, nodeType)

    override fun toString(): String {
        return "${javaClass.simpleName}($elementType)"
    }
        
}