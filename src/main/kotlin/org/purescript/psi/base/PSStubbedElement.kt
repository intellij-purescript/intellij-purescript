package org.purescript.psi.base

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.parentOfType
import org.purescript.psi.module.Module

abstract class PSStubbedElement<Stub : StubElement<*>> :
    StubBasedPsiElement<Stub>,
    StubBasedPsiElementBase<Stub> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    val module get() = parentOfType<Module.Psi>()
    override fun toString() = "${javaClass.simpleName}($elementType)"

    inline fun <Stub : StubElement<Out>, reified Out : StubBasedPsiElement<Stub>>
        children(childType: IStubElementType<Stub, Out>): Array<Out> =
        getStubOrPsiChildren(childType, arrayOf<Out>())

    inline fun <reified Psi : PSStubbedElement<*>> children(): Array<Psi> {
        return greenStub
            ?.childrenStubs
            ?.map { it.psi }
            ?.filterIsInstance<Psi>()
            ?.toTypedArray()
            ?: findChildrenByClass()
    }

    inline fun <reified P : PSStubbedElement<*>> child() =
        children<P>().firstOrNull()

    inline fun <reified Psi : PsiElement> findChildrenByClass():
        Array<Psi> = `access$findChildrenByClass`(Psi::class.java)

    // Todo clean up
    @PublishedApi
    internal fun <T : Any?> `access$findChildrenByClass`(aClass: Class<T>?) =
        super.findChildrenByClass(aClass)    // Todo clean up
}