package org.purescript.module.declaration.value.expression.identifier

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.module.declaration.type.LabeledIndex
import org.purescript.module.declaration.value.Similar
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiElement

class PSAccessor(node: ASTNode) : PSPsiElement(node), Similar {
    private val identifier get() = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName(): String = identifier.name
    override fun getReference(): PsiReference = AccessorReference(this)
    
    class AccessorReference(val accessor: PSAccessor) : PsiReferenceBase<PSAccessor>(
        accessor,
        accessor.textRangeInParent,
        false
    ) {
        override fun resolve(): PsiElement? {
            val scope = GlobalSearchScope.allScope(accessor.project)
            val index = LabeledIndex
            val name = accessor.name
            val candidates = index.get(name, accessor.project, scope)
            return candidates.singleOrNull()
        }
    }
}