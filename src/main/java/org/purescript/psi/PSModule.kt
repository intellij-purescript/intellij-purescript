package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import org.purescript.features.DocCommentOwner
import org.purescript.psi.import.PSImportDeclarationImpl


class PSModule(node: ASTNode) :
    PSPsiElement(node),
    PsiNameIdentifierOwner,
    DocCommentOwner {
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
     * If the export list is null, this module implicitly exports all its members.
     */
    val exportList: PSExportList? = findChildByClass(PSExportList::class.java)

    /**
     * The foreign values declared in this module
     */
    val foreignValueDeclarations: Array<PSForeignValueDeclaration>
        get() =
            findChildrenByClass(PSForeignValueDeclaration::class.java)

    val importDeclarations: Array<PSImportDeclarationImpl>
        get() =
            findChildrenByClass(PSImportDeclarationImpl::class.java)

    val valueDeclarations: Array<PSValueDeclaration>
        get() = findChildrenByClass(PSValueDeclaration::class.java)

    val exportedValueDeclarations: List<PSValueDeclaration>
        get() {
            val explicitlyExportedItems = exportList?.exportedItems
                ?: return valueDeclarations.toList()

            val explicitlyExportedValueNames = explicitlyExportedItems
                .filterIsInstance<PSExportedValue>()
                .map { it.name }
                .toSet()

            val exportedValueDeclarations = mutableListOf<PSValueDeclaration>()
            valueDeclarations.filterTo(exportedValueDeclarations) {
                it.name in explicitlyExportedValueNames
            }

            explicitlyExportedItems.filterIsInstance<PSExportedModule>()
                .mapNotNull { it.importDeclaration }
                .flatMapTo(exportedValueDeclarations) { it.importedValues }

            return exportedValueDeclarations
        }

    private val valuesFromReexportedModules
        get() =
            importDeclarations
                .filter { it.name in reexportedModuleNames }
                .flatMap { it.importedValues }
                .asSequence()

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

    override val docComments: List<PsiComment>
        get() = getDocComments()

}
