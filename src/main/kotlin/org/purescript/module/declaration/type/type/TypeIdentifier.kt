package org.purescript.module.declaration.type.type

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.parentsOfType
import org.purescript.inference.Inferable
import org.purescript.module.declaration.type.TypeNamespace
import org.purescript.name.PSIdentifier
import org.purescript.psi.PSPsiElement
import org.purescript.psi.PSPsiFactory

class TypeIdentifier(node: ASTNode) : PSPsiElement(node), PSType {
    val identifier = findNotNullChildByClass(PSIdentifier::class.java)
    override fun getName() = identifier.name
    override fun getReference() = object : PsiReferenceBase<TypeIdentifier>(this, identifier.textRangeInParent, false) {
        private val candidates = element.parentsOfType<TypeNamespace>().flatMap { it.typeNames }
        override fun getVariants() = candidates.toList().toTypedArray()
        override fun resolve(): PsiNamedElement? {
            val list = candidates.toList()
            return list.firstOrNull { it.name == element.name }
        }

        override fun handleElementRename(name: String): PsiElement? {
            val oldName = element.identifier
            val newName = PSPsiFactory(element.project).createIdentifier(name) ?: return null
            oldName.replace(newName)
            return element
        }
    }

    override fun unify() {
        (reference.resolve() as? Inferable)?.inferType()?.let { unify(it) }
    }
}