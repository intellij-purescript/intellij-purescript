package org.purescript.inference

import com.intellij.psi.PsiElement
import java.util.WeakHashMap

class TypeSpace {
    fun newId(): InferType.Id = idGenerator.newId()
    fun substitute(type: InferType): InferType = substitutions.substitute(type)
    fun unify(x: InferType, y: InferType) = substitutions.unify(x, y)
    fun typeIdOf(descendant: PsiElement): InferType.Id = typeMap.getOrPut(descendant, ::newId)
    fun replaceMap(): (InferType.Id) -> InferType.Id {
        val map = mutableMapOf<InferType.Id, InferType.Id>()
        return { map.getOrPut(it, ::newId) }
    }

    val idGenerator = IdGenerator()
    val substitutions = mutableMapOf<InferType.Id, InferType>()
    val typeMap = WeakHashMap<PsiElement, InferType.Id>()
}