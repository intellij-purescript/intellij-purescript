package org.purescript.psi.imports

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.*
import org.purescript.psi.PSElementType.*
import org.purescript.psi.declaration.classes.PSClassDeclaration
import org.purescript.psi.declaration.classes.PSClassMember
import org.purescript.psi.declaration.data.DataConstructor.Psi
import org.purescript.psi.declaration.data.DataDeclaration
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.value.PSValueDeclaration
import org.purescript.psi.foreign.PSForeignDataDeclaration
import org.purescript.psi.foreign.PSForeignValueDeclaration
import org.purescript.psi.module.Module
import org.purescript.psi.module.ModuleReference
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.declaration.newtype.PSNewTypeConstructor
import org.purescript.psi.declaration.newtype.PSNewTypeDeclaration
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.data.DataConstructor
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
interface Import {
    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)
    object Type : WithPsiAndStub<Stub, Psi>("ImportDeclaration") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(my: Psi, p: StubElement<*>?) = Stub(my.name, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = Unit
        override fun serialize(stub: Stub, d: StubOutputStream) =
            d.writeName(stub.name)

        override fun deserialize(d: StubInputStream, p: StubElement<*>?): Stub =
            Stub(d.readNameString()!!, p)
    }

    class Psi : PSStubbedElement<Stub>, Comparable<Psi> {
        constructor(node: ASTNode) : super(node)
        constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)

        override fun toString() = "PSImportDeclaration($elementType)"

        /**
         * The identifier specifying module being imported, e.g.
         *
         * `Foo.Bar` in
         * ```
         * import Foo.Bar as Bar
         * ```
         */
        val moduleName: PSModuleName
            get() = findChildByClass(PSModuleName::class.java)!!

        /**
         * The import list of this import declaration.
         * It being null implies that all items are imported.
         */
        val importList: PSImportList?
            get() =
                findChildByClass(PSImportList::class.java)

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
            importAlias?.name ?: moduleName.name

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
         * @return a reference to the [Psi] that this declaration is
         * importing from
         */
        override fun getReference(): ModuleReference =
            ModuleReference(this)

        override fun compareTo(other: Psi): Int {
            return when {
                name == "Prelude" && other.name != "Prelude" -> -1
                name != "Prelude" && other.name == "Prelude" -> 1
                else -> compareValuesBy(
                    this,
                    other,
                    { it.moduleName.name },
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
         * @return the [Declaration] elements that this declaration imports
         */
        private inline fun <Declaration : PsiNamedElement, reified Wanted : PSImportedItem>
            getImportedDeclarations(exportedDeclarationProperty: KProperty1<Module.Psi, List<Declaration>>): List<Declaration> {
            val importedModule = importedModule ?: return emptyList()
            val exportedDeclarations =
                exportedDeclarationProperty.get(importedModule)

            val importedItems = importList?.importedItems
                ?: return exportedDeclarations

            val importedNames =
                importedItems.filterIsInstance(Wanted::class.java)
                    .map { it.name }
                    .toSet()

            return if (isHiding) {
                exportedDeclarations.filter { it.name !in importedNames }
            } else {
                exportedDeclarations.filter { it.name in importedNames }
            }
        }

        /**
         * @return the [Module.Psi] that this declaration is importing from
         */
        val importedModule get(): Module.Psi? = reference.resolve()

        /**
         * @return the [PSValueDeclaration] elements imported by this declaration
         */
        val importedValueDeclarations: List<PSValueDeclaration>
            get() = getImportedDeclarations<PSValueDeclaration, PSImportedValue>(
                Module.Psi::exportedValueDeclarations
            )

        /**
         * @return the [PSForeignValueDeclaration] elements imported by this declaration
         */
        val importedForeignValueDeclarations: List<PSForeignValueDeclaration>
            get() = getImportedDeclarations<PSForeignValueDeclaration, PSImportedValue>(
                Module.Psi::exportedForeignValueDeclarations
            )

        /**
         * @return the [PSForeignDataDeclaration] elements imported by this declaration
         */
        val importedForeignDataDeclarations: List<PSForeignDataDeclaration>
            get() = getImportedDeclarations<PSForeignDataDeclaration, PSImportedData>(
                Module.Psi::exportedForeignDataDeclarations
            )

        /**
         * @return the [PSNewTypeDeclaration] elements imported by this declaration
         */
        val importedNewTypeDeclarations: List<PSNewTypeDeclaration>
            get() = getImportedDeclarations<PSNewTypeDeclaration, PSImportedData>(
                Module.Psi::exportedNewTypeDeclarations
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
                    exportedNewTypeConstructors.filterTo(
                        importedNewTypeConstructors
                    ) {
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
         * @return the [DataDeclaration.Psi] elements imported by this declaration
         */
        val importedDataDeclarations: List<DataDeclaration.Psi>
            get() = getImportedDeclarations<DataDeclaration.Psi, PSImportedData>(
                Module.Psi::exportedDataDeclarations
            )

        /**
         * @return the [DataConstructor.Psi] elements imported by this declaration
         */
        val importedDataConstructors: List<DataConstructor.Psi>
            get() {
                val importedModule = importedModule ?: return emptyList()
                val exportedDataConstructors =
                    importedModule.exportedDataConstructors

                val importedItems = importList?.importedItems
                    ?: return exportedDataConstructors

                val importedDataConstructors =
                    mutableListOf<DataConstructor.Psi>()
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
                Module.Psi::exportedTypeSynonymDeclarations
            )

        /**
         * @return the [PSClassDeclaration] elements imported by this declaration
         */
        val importedClassDeclarations: List<PSClassDeclaration>
            get() = getImportedDeclarations<PSClassDeclaration, PSImportedClass>(
                Module.Psi::exportedClassDeclarations
            )

        /**
         * @return the [PSClassMember] elements imported by this declaration
         */
        val importedClassMembers: List<PSClassMember>
            get() = getImportedDeclarations<PSClassMember, PSImportedValue>(
                Module.Psi::exportedClassMembers
            )

        /**
         * @return the [org.purescript.psi.declaration.FixityDeclaration.Psi] elements imported by this declaration
         */
        val importedFixityDeclarations
            get() =
                getImportedDeclarations<FixityDeclaration.Psi, PSImportedOperator>(
                    Module.Psi::exportedFixityDeclarations
                )

    }
}
