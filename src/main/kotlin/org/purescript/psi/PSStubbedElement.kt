package org.purescript.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
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
    
    inline fun <Stub: StubElement<Out>, reified Out: StubBasedPsiElement<Stub>>
        children(childType: IStubElementType<Stub, Out>): Array<Out> =
        getStubOrPsiChildren(childType, arrayOf<Out>())
    
    inline fun <reified Psi : PsiElement?> children(): Array<Psi> {
        return greenStub
            ?.childrenStubs
            ?.map { it.psi }
            ?.filterIsInstance<Psi>() 
            ?.toTypedArray()
            ?: `access$findChildrenByClass`(Psi::class.java) 
    }

    // Todo clean up
    @PublishedApi
    internal fun <T : Any?> `access$findChildrenByClass`(aClass: Class<T>?) =
        findChildrenByClass(aClass)
}