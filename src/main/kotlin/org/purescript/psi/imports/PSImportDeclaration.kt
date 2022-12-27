package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import org.purescript.psi.ModuleReference
import org.purescript.psi.PSForeignDataDeclaration
import org.purescript.psi.PSForeignValueDeclaration
import org.purescript.psi.PSPsiElement
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.ExportedFixityDeclarationsIndex
import org.purescript.psi.declaration.PSFixityDeclaration
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.module.PSModule
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclaration
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
class PSImportDeclaration(node: ASTNode) : PSPsiElement(node),
    Comparable<PSImportDeclaration> {

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
     * @return true if the import declaration implicitly imports
     * all available items.
     */
    val importsAll: Boolean
        get() = importList == null

    /**
     * @return all the items in [importList], or an empty array if
     * the import list is null.
     */
    val importedItems: Array<PSImportedItem>
        get() =
            importList?.importedItems ?: emptyArray()

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

    override fun compareTo(other: PSImportDeclaration): Int {
        return when {
            name == "Prelude" && other.name != "Prelude" -> -1
            name != "Prelude" && other.name == "Prelude" -> 1
            else -> compareValuesBy(
                this,
                other,
                { it.moduleName?.name },
                { it.isHiding },
                { it.importAlias?.name },
                { it.text } // TODO We probably want to compare by import list instead
            )
        }
    }

    /**
     * Helper method for retrieving various types of imported declarations.
     *
     * @param exportedDeclarationProperty The property for the exported declarations of the wanted type in the module
     * @param importedItemClass The class of the [PSImportedItem] to use when filtering the results
     * @return the [Declaration] elements that this declaration imports
     */
    private inline fun <Declaration : PsiNamedElement, reified Wanted : PSImportedItem>
        getImportedDeclarations(exportedDeclarationProperty: KProperty1<PSModule, List<Declaration>>): List<Declaration> {
        val importedModule = importedModule ?: return emptyList()
        val exportedDeclarations =
            exportedDeclarationProperty.get(importedModule)

        val importedItems = importList?.importedItems
            ?: return exportedDeclarations

        val importedNames = importedItems.filterIsInstance(Wanted::class.java)
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
        get() = getImportedDeclarations<PSValueDeclaration, PSImportedValue>(
            PSModule::exportedValueDeclarations
        )

    /**
     * @return the [PSForeignValueDeclaration] elements imported by this declaration
     */
    val importedForeignValueDeclarations: List<PSForeignValueDeclaration>
        get() = getImportedDeclarations<PSForeignValueDeclaration, PSImportedValue>(
            PSModule::exportedForeignValueDeclarations
        )

    /**
     * @return the [PSForeignDataDeclaration] elements imported by this declaration
     */
    val importedForeignDataDeclarations: List<PSForeignDataDeclaration>
        get() = getImportedDeclarations<PSForeignDataDeclaration, PSImportedData>(
            PSModule::exportedForeignDataDeclarations
        )

    /**
     * @return the [PSNewTypeDeclaration] elements imported by this declaration
     */
    val importedNewTypeDeclarations: List<PSNewTypeDeclaration>
        get() = getImportedDeclarations<PSNewTypeDeclaration, PSImportedData>(
            PSModule::exportedNewTypeDeclarations
        )

    /**
     * @return the [PSNewTypeConstructor] elements imported by this declaration
     */
    val importedNewTypeConstructors: List<PSNewTypeConstructor>
        get() {
            val importedModule = importedModule ?: return emptyList()
            val exportedNewTypeConstructors =
                importedModule.exportedNewTypeConstructors

            val importedItems = importList?.importedItems
                ?: return exportedNewTypeConstructors

            val importedNewTypeConstructors =
                mutableListOf<PSNewTypeConstructor>()
            val importedDataElements =
                importedItems.filterIsInstance<PSImportedData>()
            if (isHiding) {
                /*
                 * Partially hiding imported data does not work in an intuitive way.
                 * Here are some examples using Data.Maybe:
                 *
                 *   import Data.Maybe hiding (Maybe(..))                           -- Hides type and constructors
                 *   import Data.Maybe hiding (Maybe)                               -- Hides nothing
                 *   import Data.Maybe hiding (Maybe())                             -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Just))                         -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Just, Nothing))                -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Nothing, Just))                -- Hides type and constructors
                 *   import Data.Maybe hiding (Maybe(Just), Maybe(Nothing))         -- Hides nothing
                 *   import Data.Maybe hiding (Maybe(Nothing), Maybe(Just))         -- Hides nothing
                 *   import Data.Maybe hiding (Maybe, Maybe(Nothing), Maybe(Just))  -- Hides nothing
                 *   import Data.Maybe hiding (Maybe, Maybe(Just), Maybe(Nothing))  -- Hides nothing
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
                    val newTypeConstructor =
                        importedData.newTypeDeclaration?.newTypeConstructor
                            ?: continue
                    if (importedData.importsAll || newTypeConstructor.name in importedData.importedDataMembers.map { it.name }) {
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
        get() = getImportedDeclarations<PSDataDeclaration, PSImportedData>(
            PSModule::exportedDataDeclarations
        )

    /**
     * @return the [PSDataConstructor] elements imported by this declaration
     */
    val importedDataConstructors: List<PSDataConstructor>
        get() {
            val importedModule = importedModule ?: return emptyList()
            val exportedDataConstructors =
                importedModule.exportedDataConstructors

            val importedItems = importList?.importedItems
                ?: return exportedDataConstructors

            val importedDataConstructors = mutableListOf<PSDataConstructor>()
            val importedDataElements =
                importedItems.filterIsInstance<PSImportedData>()
            if (isHiding) {
                // TODO See todo in [importedNewTypeConstructors]
                val hiddenDataConstructors = importedDataElements
                    .filter { it.importsAll }
                    .mapNotNull { it.dataDeclaration }
                    .flatMap { it.dataConstructors.toList() }
                exportedDataConstructors.filterTo(importedDataConstructors) { it !in hiddenDataConstructors }
            } else {
                for (importedData in importedDataElements) {
                    val dataConstructors =
                        importedData.dataDeclaration?.dataConstructors
                            ?: continue
                    if (importedData.importsAll) {
                        importedDataConstructors.addAll(dataConstructors)
                    } else {
                        val importedDataConstructorNames =
                            importedData.importedDataMembers.map { it.name }
                        dataConstructors.filterTo(importedDataConstructors) { it.name in importedDataConstructorNames }
                    }
                }
            }

            return importedDataConstructors
        }

    /**
     * @return the [PSTypeSynonymDeclaration] elements imported by this declaration
     */
    val importedTypeSynonymDeclarations: List<PSTypeSynonymDeclaration>
        get() = getImportedDeclarations<PSTypeSynonymDeclaration, PSImportedData>(
            PSModule::exportedTypeSynonymDeclarations
        )

    /**
     * @return the [PSClassDeclaration] elements imported by this declaration
     */
    val importedClassDeclarations: List<PSClassDeclaration>
        get() = getImportedDeclarations<PSClassDeclaration, PSImportedClass>(
            PSModule::exportedClassDeclarations
        )

    /**
     * @return the [PSClassMember] elements imported by this declaration
     */
    val importedClassMembers: List<PSClassMember>
        get() = getImportedDeclarations<PSClassMember, PSImportedValue>(
            PSModule::exportedClassMembers
        )

    /**
     * @return the [org.purescript.psi.declaration.PSFixityDeclaration] elements imported by this declaration
     */
    val importedFixityDeclarations
        get() =
            getImportedDeclarations<PSFixityDeclaration, PSImportedOperator>(
                PSModule::exportedFixityDeclarations
            )

}
