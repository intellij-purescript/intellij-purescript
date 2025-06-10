package org.purescript.inference

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents

class InferableTypeProvider: ExpressionTypeProvider<Inferable>() {
    override fun getInformationHint(element: Inferable): String = "${element.inferType()}"
    override fun getErrorHint(): String = "no hint found"
    override fun getExpressionsAt(elementAt: PsiElement): MutableList<Inferable> {
        val ret = mutableListOf<Inferable>()
        for (parent in elementAt.parents(true)) {
            (parent as? Inferable)?.let { ret.add(it) }
        }
        return ret.distinctBy { it.textRange }.toMutableList()
    }
    
}