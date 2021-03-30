package org.purescript.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import org.purescript.features.DocCommentOwner
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.exports.*
import org.purescript.psi.imports.PSImportDeclarationImpl
import org.purescript.psi.newtype.PSNewTypeDeclarationImpl
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration
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

    override fun getTextOffset(): Int = nameIdentifier.textOffset

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
     * @return the [Declaration] element that this module exports
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
     * @return the [PSExportList] in this module, if it exists
     */
    val exportList: PSExportList? = findChildByClass(PSExportList::class.java)

    /**
     * @return the [PSImportDeclarationImpl] elements in this module
     */
    val importDeclarations: Array<PSImportDeclarationImpl>
        get() =
            findChildrenByClass(PSImportDeclarationImpl::class.java)

    /**
     * @return the [PSValueDeclaration] elements in this module
     */
    val valueDeclarations: Array<PSValueDeclaration>
        get() = findChildrenByClass(PSValueDeclaration::class.java)

    /**
     * @return the [PSForeignValueDeclaration] elements in this module
     */
    val foreignValueDeclarations: Array<PSForeignValueDeclaration>
        get() =
            findChildrenByClass(PSForeignValueDeclaration::class.java)

    /**
     * @return the [PSNewTypeDeclarationImpl] elements in this module
     */
    val newTypeDeclarations: Array<PSNewTypeDeclarationImpl>
        get() =
            findChildrenByClass(PSNewTypeDeclarationImpl::class.java)

    /**
     * @return the [PSDataDeclaration] elements in this module
     */
    val dataDeclarations: Array<PSDataDeclaration>
        get() =
            findChildrenByClass(PSDataDeclaration::class.java)

    /**
     * @return the [PSTypeSynonymDeclaration] elements in this module
     */
    val typeSynonymDeclarations: Array<PSTypeSynonymDeclaration>
        get() =
            findChildrenByClass(PSTypeSynonymDeclaration::class.java)

    /**
     * @return the [PSClassDeclaration] elements in this module
     */
    val classDeclarations: Array<PSClassDeclaration>
        get() =
            findChildrenByClass(PSClassDeclaration::class.java)

    /**
     * @return the [PSValueDeclaration] that this module exports,
     * both directly and through re-exported modules
     */
    val exportedValueDeclarations: List<PSValueDeclaration>
        get() = getExportedDeclarations(
            valueDeclarations,
            PSImportDeclarationImpl::importedValueDeclarations,
            PSExportedValue::class.java
        )

    /**
     * @return the [PSForeignValueDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedForeignValueDeclarations: List<PSForeignValueDeclaration>
        get() = getExportedDeclarations(
            foreignValueDeclarations,
            PSImportDeclarationImpl::importedForeignValueDeclarations,
            PSExportedValue::class.java
        )

    /**
     * @return the [PSNewTypeDeclarationImpl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedNewTypeDeclarations: List<PSNewTypeDeclarationImpl>
        get() = getExportedDeclarations(
            newTypeDeclarations,
            PSImportDeclarationImpl::importedNewTypeDeclarations,
            PSExportedData::class.java
        )

    /**
     * @return the [PSDataDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedDataDeclarations: List<PSDataDeclaration>
        get() = getExportedDeclarations(
            dataDeclarations,
            PSImportDeclarationImpl::importedDataDeclarations,
            PSExportedData::class.java
        )

    /**
     * @return the [PSTypeSynonymDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedTypeSynonymDeclarations: List<PSTypeSynonymDeclaration>
        get() = getExportedDeclarations(
            typeSynonymDeclarations,
            PSImportDeclarationImpl::importedTypeSynonymDeclarations,
            PSExportedData::class.java
        )

    /**
     * @return the [PSClassDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedClassDeclarations: List<PSClassDeclaration>
        get() = getExportedDeclarations(
            classDeclarations,
            PSImportDeclarationImpl::importedClassDeclarations,
            PSExportedClass::class.java
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
