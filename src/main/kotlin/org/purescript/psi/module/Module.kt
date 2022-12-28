package org.purescript.psi.module

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.*
import com.intellij.util.containers.addIfNotNull
import org.purescript.features.DocCommentOwner
import org.purescript.parser.FixityDeclarationType
import org.purescript.parser.WHERE
import org.purescript.psi.*
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.FixityDeclaration
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.exports.*
import org.purescript.psi.imports.PSImportDeclaration
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclaration
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration

interface Module {
    object Type : PSElementType.WithPsiAndStub<Stub, Psi>("Module") {
        override fun createPsi(node: ASTNode) = Psi(node)
        override fun createPsi(stub: Stub) = Psi(stub, this)
        override fun createStub(psi: Psi, p: StubElement<*>) = Stub(psi.name, p)
        override fun serialize(stub: Stub, data: StubOutputStream) {
            data.writeName(stub.name)
        }

        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            sink.occurrence(ModuleNameIndex.KEY, stub.name)
        }

    }

    class Stub(val name: String, p: StubElement<*>?) : AStub<Psi>(p, Type)

    class Psi :
        PsiNameIdentifierOwner,
        DocCommentOwner,
        PSStubbedElement<Stub> {
        constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)
        constructor(node: ASTNode) : super(node)
        // TODO clean up this name
        override fun toString(): String = "PSModule($elementType)"
        var cache: Cache = Cache()

        inner class Cache {
            val name: String by lazy { nameIdentifier.name }
            val exports by lazy { findChildByClass(PSExportList::class.java) }
            val imports: Array<PSImportDeclaration>
                by lazy { findChildrenByClass(PSImportDeclaration::class.java) }
            val importsByName by lazy { imports.groupBy { it.name } }
            val valueDeclarations: Array<PSValueDeclaration>
                by lazy { findChildrenByClass(PSValueDeclaration::class.java) }

            val dataDeclarations: Array<PSDataDeclaration>
                by lazy { findChildrenByClass(PSDataDeclaration::class.java) }
            val dataConstructors
                by lazy { dataDeclarations.flatMap { it.dataConstructors.toList() } }

            val newTypeDeclarations: Array<PSNewTypeDeclaration>
                by lazy { findChildrenByClass(PSNewTypeDeclaration::class.java) }
            val newTypeConstructors: List<PSNewTypeConstructor>
                by lazy { newTypeDeclarations.map { it.newTypeConstructor } }

            val typeSynonymDeclarations: Array<PSTypeSynonymDeclaration>
                by lazy { findChildrenByClass(PSTypeSynonymDeclaration::class.java) }

            val classes: Array<PSClassDeclaration>
                by lazy { findChildrenByClass(PSClassDeclaration::class.java) }

            val fixityDeclarations by lazy { children(FixityDeclarationType) }

            val foreignValueDeclarations: Array<PSForeignValueDeclaration>
                by lazy { findChildrenByClass(PSForeignValueDeclaration::class.java) }
            val foreignDataDeclarations: Array<PSForeignDataDeclaration>
                by lazy { findChildrenByClass(PSForeignDataDeclaration::class.java) }
        }

        override fun subtreeChanged() {
            cache = Cache()
            super.subtreeChanged()
        }

        override fun getName(): String = stub?.name ?: cache.name

        override fun setName(name: String): PsiElement? {
            val properName = PSPsiFactory(project).createModuleName(name)
                ?: return null
            nameIdentifier.replace(properName)
            return this
        }

        override fun getNameIdentifier(): PSModuleName {
            return findNotNullChildByClass(PSModuleName::class.java)
        }

        override fun getTextOffset(): Int = nameIdentifier.textOffset

        /**
         * @return the [FixityDeclaration.Psi] that this module exports,
         * both directly and through re-exported modules
         */
        val exportedFixityDeclarations: List<FixityDeclaration.Psi>
            get() = getExportedDeclarations<FixityDeclaration.Psi, PSExportedOperator>(
                cache.fixityDeclarations
            ) { it.importedFixityDeclarations }

        /**
         * @return the where keyword in the module header
         */
        val whereKeyword: PsiElement
            get() = findNotNullChildByType(WHERE)

        /**
         * Helper method for retrieving various types of exported declarations.
         *
         * @param declarations The declarations of the wanted type in this module
         * @return the [Declaration] element that this module exports
         */
        private inline fun <Declaration : PsiNamedElement, reified Wanted : PSExportedItem> getExportedDeclarations(
            declarations: Array<Declaration>,
            getDeclarations: (PSImportDeclaration) -> List<Declaration>
        ): List<Declaration> {
            val explicitlyExportedItems = cache.exports?.exportedItems
            return if (explicitlyExportedItems == null) {
                declarations.toList()
            } else {
                val explicitlyNames = explicitlyExportedItems
                    .filterIsInstance(Wanted::class.java)
                    .map { it.name }
                    .toSet()

                val exportsSelf = explicitlyExportedItems
                    .filterIsInstance<PSExportedModule>()
                    .any { it.name == name }

                val exportedDeclarations = mutableListOf<Declaration>()

                declarations.filterTo(exportedDeclarations) {
                    exportsSelf || it.name in explicitlyNames
                }

                explicitlyExportedItems.filterIsInstance<PSExportedModule>()
                    .flatMap { it.importDeclarations }
                    .flatMapTo(exportedDeclarations) {
                        getDeclarations(it)
                    }
                exportedDeclarations
            }
        }

        /**
         * @return the [PSValueDeclaration] that this module exports,
         * both directly and through re-exported modules
         */
        val exportedValueDeclarations: List<PSValueDeclaration>
            get() = getExportedDeclarations<PSValueDeclaration, PSExportedValue>(
                cache.valueDeclarations,
            ) { it.importedValueDeclarations }

        /**
         * @return the [PSForeignValueDeclaration] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedForeignValueDeclarations: List<PSForeignValueDeclaration>
            get() = getExportedDeclarations<PSForeignValueDeclaration, PSExportedValue>(
                cache.foreignValueDeclarations,
            ) { it.importedForeignValueDeclarations }

        /**
         * @return the [PSForeignDataDeclaration] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedForeignDataDeclarations: List<PSForeignDataDeclaration>
            get() = getExportedDeclarations<PSForeignDataDeclaration, PSExportedData>(
                cache.foreignDataDeclarations,
            ) { it.importedForeignDataDeclarations }

        /**
         * @return the [PSNewTypeDeclaration] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedNewTypeDeclarations: List<PSNewTypeDeclaration>
            get() = getExportedDeclarations<PSNewTypeDeclaration, PSExportedData>(
                cache.newTypeDeclarations,
            ) { it.importedNewTypeDeclarations }

        /**
         * @return the [PSNewTypeConstructor] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedNewTypeConstructors: List<PSNewTypeConstructor>
            get() {
                val explicitlyExportedItems = cache.exports?.exportedItems
                    ?: return cache.newTypeConstructors

                val exportedNewTypeConstructors =
                    mutableListOf<PSNewTypeConstructor>()

                for (exportedData in explicitlyExportedItems.filterIsInstance<PSExportedData>()) {
                    if (exportedData.exportsAll) {
                        exportedNewTypeConstructors.addIfNotNull(exportedData.newTypeDeclaration?.newTypeConstructor)
                    } else {
                        exportedData.dataMembers
                            .mapNotNull { it.reference.resolve() }
                            .filterIsInstanceTo(exportedNewTypeConstructors)
                    }
                }

                explicitlyExportedItems.filterIsInstance<PSExportedModule>()
                    .flatMap { it.importDeclarations }
                    .flatMapTo(exportedNewTypeConstructors) { it.importedNewTypeConstructors }

                return exportedNewTypeConstructors
            }

        /**
         * @return the [PSDataDeclaration] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedDataDeclarations: List<PSDataDeclaration>
            get() = getExportedDeclarations<PSDataDeclaration, PSExportedData>(
                cache.dataDeclarations,
            ) { it.importedDataDeclarations }

        /**
         * @return the [PSDataConstructor] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedDataConstructors: List<PSDataConstructor>
            get() {
                val explicitlyExportedItems = cache.exports?.exportedItems
                    ?: return cache.dataConstructors

                val exportedDataConstructors =
                    mutableListOf<PSDataConstructor>()

                for (exportedData in explicitlyExportedItems.filterIsInstance<PSExportedData>()) {
                    if (exportedData.exportsAll) {
                        exportedData.dataDeclaration?.dataConstructors
                            ?.mapTo(exportedDataConstructors) { it }
                    } else {
                        exportedData.dataMembers
                            .mapNotNull { it.reference.resolve() }
                            .filterIsInstanceTo(exportedDataConstructors)
                    }
                }

                explicitlyExportedItems.filterIsInstance<PSExportedModule>()
                    .flatMap { it.importDeclarations }
                    .flatMapTo(exportedDataConstructors) { it.importedDataConstructors }

                return exportedDataConstructors
            }

        /**
         * @return the [PSTypeSynonymDeclaration] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedTypeSynonymDeclarations: List<PSTypeSynonymDeclaration>
            get() = getExportedDeclarations<PSTypeSynonymDeclaration, PSExportedData>(
                cache.typeSynonymDeclarations,
            ) { it.importedTypeSynonymDeclarations }

        /**
         * @return the [PSClassDeclaration] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedClassDeclarations: List<PSClassDeclaration>
            get() = getExportedDeclarations<PSClassDeclaration, PSExportedClass>(
                cache.classes,
            ) { it.importedClassDeclarations }

        /**
         * @return the [PSClassMember] elements that this module exports,
         * both directly and through re-exported modules
         */
        val exportedClassMembers: List<PSClassMember>
            get() = getExportedDeclarations<PSClassMember, PSExportedValue>(
                cache.classes
                    .flatMap { it.classMembers.asSequence() }
                    .toTypedArray(),
            ) { it.importedClassMembers }

        val reexportedModuleNames: List<String>
            get() =
                cache.exports?.exportedItems
                    ?.filterIsInstance(PSExportedModule::class.java)
                    ?.map { it.name }
                    ?.toList()
                    ?: emptyList()

        val exportedNames: List<String>
            get() =
                cache.exports?.exportedItems
                    ?.filter { it !is PSExportedModule }
                    ?.map { it.text.trim() }
                    ?.toList()
                    ?: emptyList()

        override val docComments: List<PsiComment>
            get() = getDocComments()

        fun addImportDeclaration(importDeclaration: PSImportDeclaration) {
            val lastImportDeclaration = cache.imports.lastOrNull()
            val insertPosition = lastImportDeclaration ?: whereKeyword
            val newLine = PSPsiFactory(project).createNewLine()
            addAfter(importDeclaration, insertPosition)
            addAfter(newLine, insertPosition)
            if (lastImportDeclaration == null) {
                addAfter(newLine, insertPosition)
            }
        }

        val exportsSelf: Boolean
            get() =
                cache.exports?.exportedItems
                    ?.filterIsInstance<PSExportedModule>()
                    ?.any { it.name == name }
                    ?: true

    }
}
