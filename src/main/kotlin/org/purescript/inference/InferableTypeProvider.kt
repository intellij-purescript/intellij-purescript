package org.purescript.inference

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents

class InferableTypeProvider<E>: ExpressionTypeProvider<E>() where E:Inferable, E:PsiElement {
    override fun getInformationHint(element: E): String = "${element.inferType()}"
    override fun getErrorHint(): String = "no hint found"
    override fun getExpressionsAt(elementAt: PsiElement): MutableList<E> {
        val ret = mutableListOf<E>()
        for (parent in elementAt.parents(true)) {
            (parent as? Inferable)?.let { ret.add(it as E) }
        }
        return ret.distinctBy { it.textRange }.toMutableList()
    }
    
}