package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import org.purescript.features.DocCommentOwner
import org.purescript.psi.imports.PSImportDeclarationImpl
import kotlin.reflect.KProperty1


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
     * Helper method for retrieving various types of exported declarations.
     *
     * @param declarations The declarations of the wanted type in this module
     * @param importedDeclarationProperty The property for the imported declarations in an [PSImportDeclarationImpl]
     * @param exportedItemClass The class of the [PSExportedItem] to use when filtering the results
     */
    private fun <Declaration : PsiNamedElement> getExportedDeclarations(
        declarations: Array<Declaration>,
        importedDeclarationProperty: KProperty1<PSImportDeclarationImpl, List<Declaration>>,
        exportedItemClass: Class<out PSExportedItem>
    ): List<Declaration> {
        val explicitlyExportedItems = exportList?.exportedItems
            ?: return declarations.toList()

        val explicitlyNames = explicitlyExportedItems
            .filterIsInstance(exportedItemClass)
            .map { it.name }
            .toSet()

        val exportedDeclarations = mutableListOf<Declaration>()
        declarations.filterTo(exportedDeclarations) {
            it.name in explicitlyNames
        }

        explicitlyExportedItems.filterIsInstance<PSExportedModule>()
            .mapNotNull { it.importDeclaration }
            .flatMapTo(exportedDeclarations) { importedDeclarationProperty.get(it) }

        return exportedDeclarations
    }

    /**
     * If the export list is null, this module implicitly exports all its members.
     */
    val exportList: PSExportList? = findChildByClass(PSExportList::class.java)

    /**
     * All import declarations in this module
     */
    val importDeclarations: Array<PSImportDeclarationImpl>
        get() =
            findChildrenByClass(PSImportDeclarationImpl::class.java)

    /**
     * All values declared in this module
     */
    val valueDeclarations: Array<PSValueDeclaration>
        get() = findChildrenByClass(PSValueDeclaration::class.java)

    /**
     * The foreign values declared in this module
     */
    val foreignValueDeclarations: Array<PSForeignValueDeclaration>
        get() =
            findChildrenByClass(PSForeignValueDeclaration::class.java)

    /**
     * The newtype declarations in this module
     */
    val newTypeDeclarations: Array<PSNewTypeDeclarationImpl>
        get() =
            findChildrenByClass(PSNewTypeDeclarationImpl::class.java)

    /**
     * All the value declarations that this module exports,
     * both directly and through re-exported modules
     */
    val exportedValueDeclarations: List<PSValueDeclaration>
        get() = getExportedDeclarations(
            valueDeclarations,
            PSImportDeclarationImpl::importedValueDeclarations,
            PSExportedValue::class.java
        )

    /**
     * All the foreign value declarations that this module exports,
     * both directly and through re-exported modules
     */
    val exportedForeignValueDeclarations: List<PSForeignValueDeclaration>
        get() = getExportedDeclarations(
            foreignValueDeclarations,
            PSImportDeclarationImpl::importedForeignValueDeclarations,
            PSExportedValue::class.java
        )

    /**
     * All the newtype declarations that this module exports,
     * both directly and through re-exported modules
     */
    val exportedNewTypeDeclarations: List<PSNewTypeDeclarationImpl>
        get() = getExportedDeclarations(
            newTypeDeclarations,
            PSImportDeclarationImpl::importedNewTypeDeclarations,
            PSExportedData::class.java
        )

    val reexportedModuleNames: List<String>
        get() =
            exportList?.exportedItems?.filterIsInstance(PSExportedModule::class.java)
                ?.map { it.name }
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
