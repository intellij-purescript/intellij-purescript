package org.purescript.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.parentOfType
import org.purescript.module.Module

abstract class PSStubbedElement<Stub : StubElement<*>> :
    StubBasedPsiElement<Stub>,
    StubBasedPsiElementBase<Stub> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    val module get() = parentOfType<Module>()
    override fun toString() = "${javaClass.simpleName}($elementType)"

    inline fun <Stub : StubElement<Out>, reified Out : StubBasedPsiElement<Stub>>
        children(childType: IStubElementType<Stub, Out>): Array<Out> =
        getStubOrPsiChildren(childType, arrayOf<Out>())

    inline fun <reified Psi : PSStubbedElement<*>> children(): Array<Psi> {
        return when(val exists = greenStub) {
            null -> findChildrenByClass()
            else -> exists
                .childrenStubs
                .map { it.psi }
                .filterIsInstance<Psi>()
                .toTypedArray()
        } 
    }

    inline fun <reified P : PSStubbedElement<*>> child() =
        children<P>().firstOrNull()

    inline fun <reified Psi : PsiElement> findChildrenByClass():
        Array<Psi> = `access$findChildrenByClass`(Psi::class.java)

    // Todo clean up
    @PublishedApi
    internal fun <T : Any?> `access$findChildrenByClass`(aClass: Class<T>?) =
        super.findChildrenByClass(aClass)    // Todo clean up

    inline fun <reified T: PsiElement>addTyped(element: T): T {
        return add(element) as T
    }
    
    val typeId get() = module?.typeIdOf(this)
}