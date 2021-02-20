package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import org.purescript.parser.PSTokens

class PSModule(node: ASTNode) : PSPsiElement(node), PsiNameIdentifierOwner {
    override fun getName(): String {
        return nameIdentifier.name
    }

    override fun setName(name: String): PsiElement? {
        return null
    }

    override fun getNameIdentifier(): PSProperName {
        return findChildByClass(PSProperName::class.java)!!
    }

    override fun getTextOffset(): Int {
        return this.nameIdentifier.textRangeInParent.startOffset
    }

    fun getImportDeclarationByName(name: String): PSImportDeclarationImpl? {
        return importDeclarations
            .asSequence()
            .find { it.name ?: "" == name }
    }

    /**
     * The export list in the module signature. If the export list is null,
     * the module implicitly exports all its members.
     *
     * Example: `(foo, bar)` in
     * ```module FooBar (foo, bar) where```
     */
    val exportList: PSExportList? = findChildByClass(PSExportList::class.java)

    val foreignValueDeclarations: Array<PSForeignValueDeclaration>
        get() =
            findChildrenByClass(PSForeignValueDeclaration::class.java)

    val importDeclarations: Array<PSImportDeclarationImpl>
        get() =
            findChildrenByClass(PSImportDeclarationImpl::class.java)

    val valueDeclarations: Sequence<PSValueDeclaration>
        get() = PsiTreeUtil
            .findChildrenOfType(this, PSValueDeclaration::class.java)
            .asSequence()
            .filterNotNull()

    val valueDeclarationsByName: Map<String, List<PSValueDeclaration>>
        get() =
            valueDeclarations.groupBy { it.name }

    val exportedValueDeclarations
        get() =
            valueDeclarations.filter { it.name in exportedNames } +
                valuesFromReexportedModules

    val exportedValueDeclarationsByName: Map<String, List<PSValueDeclaration>>
        get() =
            exportedValueDeclarations.groupBy { it.name }

    private val valuesFromReexportedModules
        get() =
            importDeclarations
                .filter { it.name in reexportedModuleNames }
                .flatMap { it.importedValues }
                .asSequence()

    fun exportedValuesExcluding(names: Set<String>): Sequence<PSValueDeclaration> {
        return exportedValueDeclarations.filter { it.name !in names }
    }

    fun exportedValuesMatching(names: Set<String>): Sequence<PSValueDeclaration> {
        return exportedValueDeclarations.filter { it.name in names }
    }

    val reexportedModuleNames: List<String>
        get() =
            exportList?.exportedItems?.filterIsInstance(PSExportedModule::class.java)
                ?.map { it.text.removePrefix("module").trim() }
                ?.toList()
                ?: emptyList()

    val exportedNames: List<String>
        get() =
            exportList?.exportedItems
                ?.filter { it !is PSExportedModule }
                ?.map { it.text.trim() }
                ?.toList()
                ?: emptyList()

    val docComments: List<PsiComment>
        get() = parent.siblings(forward = false, withSelf = false)
            .filter { it.elementType == PSTokens.DOC_COMMENT }
            .filterIsInstance(PsiComment::class.java)
            .toList()
            .reversed()

    val importedValueDeclarations
        get() =
            importDeclarations.asSequence().flatMap { it.importedValues }
}
