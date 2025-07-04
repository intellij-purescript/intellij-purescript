package org.purescript.psi

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.parentOfType
import org.purescript.file.PSFile
import org.purescript.inference.HasTypeId
import org.purescript.inference.InferType
import org.purescript.module.Module

abstract class PSStubbedElement<Stub : StubElement<*>> :
    StubBasedPsiElement<Stub>,
    StubBasedPsiElementBase<Stub>,
    HasTypeId {
    constructor(node: ASTNode) : super(node)
    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

    val module get() = parentOfType<Module>() ?: error("failed to parse module in file ${containingFile.name}")
    override fun toString() = "${javaClass.simpleName}"
    inline fun <Stub : StubElement<Out>, reified Out : StubBasedPsiElement<Stub>>
        children(childType: IStubElementType<Stub, Out>): Array<Out> =
        getStubOrPsiChildren(childType as IElementType, arrayOf<Out>())

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

    inline fun <reified T: PsiElement>addTyped(element: T): T = add(element) as T
    override val typeId get() = (module.containingFile as PSFile).typeSpace.typeIdOf(this)
    override val substitutedType: InferType get() = (module.containingFile as PSFile).typeSpace.substitute(typeId)
    fun unify(other: InferType) {
        (module.containingFile as PSFile).typeSpace.unify(substitutedType, other)
    }
}