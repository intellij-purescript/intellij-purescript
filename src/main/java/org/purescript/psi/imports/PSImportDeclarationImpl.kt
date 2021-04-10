package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import org.purescript.psi.*
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclarationImpl
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration
import kotlin.reflect.KProperty1

/**
 * An import declaration, as found near the top a module.
 *
 * E.g.
 * ```
 * import Foo.Bar hiding (a, b, c) as FB
 * ```
 */
class PSImportDeclarationImpl(node: ASTNode) : PSPsiElement(node) {

    /**
     * The identifier specifying module being imported, e.g.
     *
     * `Foo.Bar` in
     * ```
     * import Foo.Bar as Bar
     * ```
     */
    val moduleName: PSModuleName?
        get() = findChildByClass(PSModuleName::class.java)

    /**
     * The import list of this import declaration.
     * It being null implies that all items are imported.
     */
    val importList: PSImportList?
        get() =
            findChildByClass(PSImportList::class.java)

    /**
     * @return the import alias of this import declaration,
     * if it has one.
     */
    val importAlias: PSImportAlias?
        get() =
            findChildByClass(PSImportAlias::class.java)

    /**
     * Either the name of the [PSImportAlias], if it exists,
     * or the name of the module this declaration is importing from.
     */
    override fun getName() =
        importAlias?.name ?: moduleName?.name

    /** the names that are exposed or hidden
     *
     * `import Lib (namedImports)`
     * */
    val namedImports: List<String>
        get() =
            importList
                ?.importedItems
                ?.map { it.name }
                ?: emptyList()

    /** is the import statement a hiding
     *
     * `import Lib hiding (x)`
     * */
    val isHiding: Boolean
        get() =
            importList?.isHiding ?: false

    /**
     * @return a reference to the [PSModule] that this declaration is
     * importing from
     */
    override fun getReference(): ModuleReference =
        ModuleReference(this)

    /**
     * Helper method for retrieving various types of imported declarations.
     *
     * @param exportedDeclarationProperty The property for the exported declarations of the wanted type in the module
     * @param importedItemClass The class of the [PSImportedItem] to use when filtering the results
     * @return the [Declaration] elements that this declaration imports
     */
    private fun <Declaration : PsiNamedElement> getImportedDeclarations(
        exportedDeclarationProperty: KProperty1<PSModule, List<Declaration>>,
        importedItemClass: Class<out PSImportedItem>
    ): List<Declaration> {
        val importedModule = importedModule ?: return emptyList()
        val exportedDeclarations = exportedDeclarationProperty.get(importedModule)

        val importedItems = importList?.importedItems
            ?: return exportedDeclarations

        val importedNames = importedItems.filterIsInstance(importedItemClass)
            .map { it.name }
            .toSet()

        return if (isHiding) {
            exportedDeclarations.filter { it.name !in importedNames }
        } else {
            exportedDeclarations.filter { it.name in importedNames }
        }
    }

    /**
     * @return the [PSModule] that this declaration is importing from
     */
    val importedModule get(): PSModule? = reference.resolve()

    /**
     * @return the [PSValueDeclaration] elements imported by this declaration
     */
    val importedValueDeclarations: List<PSValueDeclaration>
        get() = getImportedDeclarations(
            PSModule::exportedValueDeclarations,
            PSImportedValue::class.java
        )

    /**
     * @return the [PSForeignValueDeclaration] elements imported by this declaration
     */
    val importedForeignValueDeclarations: List<PSForeignValueDeclaration>
        get() = getImportedDeclarations(
            PSModule::exportedForeignValueDeclarations,
            PSImportedValue::class.java
        )

    /**
     * @return the [PSNewTypeDeclarationImpl] elements imported by this declaration
     */
    val importedNewTypeDeclarations: List<PSNewTypeDeclarationImpl>
        get() = getImportedDeclarations(
            PSModule::exportedNewTypeDeclarations,
            PSImportedData::class.java
        )

    /**
     * @return the [PSNewTypeConstructor] elements imported by this declaration
     */
    val importedNewTypeConstructors: List<PSNewTypeConstructor>
        get() {
            val importedModule = importedModule ?: return emptyList()
            val exportedNewTypeConstructors = importedModule.exportedNewTypeConstructors

            val importedItems = importList?.importedItems
                ?: return exportedNewTypeConstructors

            val importedNewTypeConstructors = mutableListOf<PSNewTypeConstructor>()
            val importedDataElements = importedItems.filterIsInstance<PSImportedData>()
            if (isHiding) {
                /*
                 * Partially hiding imported data does not work in an intuitive way.
                 * Here are some examples using Data.Maybe:
                 *
                 *   import Data.Maybe hiding (Maybe(..))             -- Hides type and constructors
                 *   import Data.Maybe hiding (Maybe)                 -- Hides nothing
                 *   import Data.Maybe hiding (Maybe())               -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Just))           -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Just, Nothing))  -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Nothing, Just))  -- Hides type and constructors
                 *
                 * TODO
                 *  I'll account for the Maybe(..) case for now, and ignore the rest.
                 *  It's definitely wrong, but I'd rather the code be simple and wrong
                 *  than complicated and still somehow wrong.
                 */
                val hiddenNewTypeConstructors = importedDataElements
                    .filter { it.importsAll }
                    .mapNotNull { it.newTypeDeclaration?.newTypeConstructor }
                exportedNewTypeConstructors.filterTo(importedNewTypeConstructors) {
                    it !in hiddenNewTypeConstructors
                }
            } else {
                for (importedData in importedDataElements) {
                    val newTypeConstructor = importedData.newTypeDeclaration?.newTypeConstructor ?: continue
                    if (importedData.importsAll || newTypeConstructor.name in importedData.importedDataMembers.map { name }) {
                        importedNewTypeConstructors.add(newTypeConstructor)
                    }
                }
            }

            return importedNewTypeConstructors
        }

    /**
     * @return the [PSDataDeclaration] elements imported by this declaration
     */
    val importedDataDeclarations: List<PSDataDeclaration>
        get() = getImportedDeclarations(
            PSModule::exportedDataDeclarations,
            PSImportedData::class.java
        )

    /**
     * @return the [PSTypeSynonymDeclaration] elements imported by this declaration
     */
    val importedTypeSynonymDeclarations: List<PSTypeSynonymDeclaration>
        get() = getImportedDeclarations(
            PSModule::exportedTypeSynonymDeclarations,
            PSImportedData::class.java
        )

    /**
     * @return the [PSClassDeclaration] elements imported by this declaration
     */
    val importedClassDeclarations: List<PSClassDeclaration>
        get() = getImportedDeclarations(
            PSModule::exportedClassDeclarations,
            PSImportedClass::class.java
        )
}
