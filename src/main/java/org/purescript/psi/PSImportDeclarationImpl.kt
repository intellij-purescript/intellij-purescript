package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.impl.source.tree.LeafPsiElement

class PSImportDeclarationImpl(node: ASTNode) : PSPsiElement(node) {

    override fun getName() = importName?.name

    /** the names that are imported or hidden
     *
     * `import Lib (namedImports)`
     * */
    val namedImports: List<String> get() =
        findChildrenByClass(PSPositionedDeclarationRefImpl::class.java)
            .asSequence()
            .map { it.text.trim() }
            .toList()

    /** is the import statement a hiding
     *
     * `import Lib hiding (x)`
     * */
    val isHiding: Boolean get() =
        SyntaxTraverser.psiTraverser(this)
            .expand { it !is PSPositionedDeclarationRefImpl }
            .filterIsInstance(LeafPsiElement::class.java)
            .any { it.text.trim() == "hiding" }


    val importName get() = findChildByClass(PSProperName::class.java)

    override fun getReference(): PsiReference {
        return ModuleReference(this)
    }

    private val importedModule get(): PSModule? = ModuleReference(this).resolve()

    val importedValues get(): List<PSValueDeclaration> =
        when {
            isHiding -> {
                importedModule?.exportedValuesExcluding(namedImports.toSet())
            }
            namedImports.isNotEmpty() -> {
                importedModule?.exportedValuesMatching(namedImports.toSet())
            }
            else -> {
                importedModule?.exportedValueDeclarations
            }
        }?: listOf()


    fun isNotHidingName(name: String): Boolean {
        return when {
            isHiding -> name !in namedImports
            namedImports.isNotEmpty() -> name in namedImports
            else -> true
        }
    }
}