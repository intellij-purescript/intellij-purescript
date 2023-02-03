package org.purescript.psi.module

import com.intellij.lang.ASTNode
import com.intellij.openapi.components.service
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.*
import com.intellij.util.containers.addIfNotNull
import org.purescript.features.DocCommentOwner
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.parser.FixityDeclType
import org.purescript.parser.WHERE
import org.purescript.psi.Importable
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.base.AStub
import org.purescript.psi.base.PSStubbedElement
import org.purescript.psi.declaration.classes.ClassDecl
import org.purescript.psi.declaration.classes.PSClassMember
import org.purescript.psi.declaration.data.DataConstructor
import org.purescript.psi.declaration.data.DataDeclaration
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.foreign.ForeignValueDecl
import org.purescript.psi.declaration.foreign.PSForeignDataDeclaration
import org.purescript.psi.declaration.imports.Import
import org.purescript.psi.declaration.newtype.NewtypeCtor
import org.purescript.psi.declaration.newtype.NewtypeDecl
import org.purescript.psi.declaration.type.TypeDecl
import org.purescript.psi.declaration.value.ValueDecl
import org.purescript.psi.declaration.value.ValueDeclarationGroup
import org.purescript.psi.exports.*
import org.purescript.psi.name.PSModuleName

class Module : PsiNameIdentifierOwner, DocCommentOwner,
    PSStubbedElement<Module.Stub>, Importable {
    object Type : PSElementType.WithPsiAndStub<Stub, Module>("Module") {
        override fun createPsi(node: ASTNode) = Module(node)
        override fun createPsi(stub: Stub) = Module(stub, this)
        override fun createStub(psi: Module, p: StubElement<*>) =
            Stub(psi.name, p)

        override fun serialize(stub: Stub, data: StubOutputStream) {
            data.writeName(stub.name)
        }

        override fun deserialize(d: StubInputStream, p: StubElement<*>?) =
            Stub(d.readNameString()!!, p)

        override fun indexStub(stub: Stub, sink: IndexSink) {
            sink.occurrence(ModuleNameIndex.KEY, stub.name)
        }

    }

    class Stub(val name: String, p: StubElement<*>?) : AStub<Module>(p, Type) {
        val exportList: ExportList.Stub?
            get() = childrenStubs
                .filterIsInstance<ExportList.Stub>()
                .firstOrNull()
    }


    constructor(stub: Stub, t: IStubElementType<*, *>) : super(stub, t)
    constructor(node: ASTNode) : super(node)

    override fun asImport() = ImportDeclaration(name)

    // TODO clean up this name
    override fun toString(): String = "PSModule($elementType)"
    var cache: Cache = Cache()

    val exports get() = child<ExportList.Psi>()
    val fixityDeclarations get() = children(FixityDeclType)

    inner class Cache {
        val imports by lazy { findChildrenByClass<Import>() }
        val importsByName by lazy { imports.groupBy { it.name } }
        val importsByModule by lazy { imports.groupBy { it.moduleName.name } }
        val valueDeclarations: Array<ValueDecl> by lazy {
            valueDeclarationGroups
                .flatMap { it.valueDeclarations.asSequence() }
                .toTypedArray()
        }
        val valueDeclarationGroups: Array<ValueDeclarationGroup>
            by lazy { findChildrenByClass<ValueDeclarationGroup>() }
        val dataDeclarations
            by lazy { findChildrenByClass<DataDeclaration.Psi>() }
        val dataConstructors
            by lazy { dataDeclarations.flatMap { it.dataConstructors.toList() } }
        val newTypeDeclarations
            by lazy { findChildrenByClass<NewtypeDecl>() }
        val newTypeConstructors: List<NewtypeCtor>
            by lazy { newTypeDeclarations.map { it.newTypeConstructor } }
        val typeSynonymDeclarations
            by lazy { findChildrenByClass<TypeDecl>() }
        val classes by lazy { findChildrenByClass<ClassDecl>() }
        val foreignValueDeclarations
            by lazy { findChildrenByClass<ForeignValueDecl>() }
        val foreignDataDeclarations
            by lazy { findChildrenByClass<PSForeignDataDeclaration>() }
    }

    override fun subtreeChanged() {
        cache = Cache()
        super.subtreeChanged()
    }

    override fun getName(): String = greenStub?.name ?: nameIdentifier.name

    override fun setName(name: String): PsiElement? {
        val properName =
            project.service<PSPsiFactory>().createModuleName(name)
                ?: return null
        nameIdentifier.replace(properName)
        return this
    }

    override fun getNameIdentifier(): PSModuleName {
        return findNotNullChildByClass(PSModuleName::class.java)
    }

    override fun getTextOffset(): Int = nameIdentifier.textOffset

    /**
     * @return the [FixityDeclaration] that this module exports,
     * both directly and through re-exported modules
     */
    val exportedFixityDeclarations: List<FixityDeclaration>
        get() = getExportedDeclarations<FixityDeclaration, ExportedOperator.Psi>(
            fixityDeclarations
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
    private inline fun <Declaration : PsiNamedElement, reified Wanted : ExportedItem<*>> getExportedDeclarations(
        declarations: Array<Declaration>,
        getDeclarations: (Import) -> List<Declaration>
    ): List<Declaration> {
        val explicitlyExportedItems = exports?.exportedItems
        return if (explicitlyExportedItems == null) {
            declarations.toList()
        } else {
            val explicitlyNames = explicitlyExportedItems
                .filterIsInstance(Wanted::class.java)
                .map { it.name }
                .toSet()

            val exportsSelf = explicitlyExportedItems
                .filterIsInstance<ExportedModule>()
                .any { it.name == name }

            val exportedDeclarations = mutableListOf<Declaration>()

            declarations.filterTo(exportedDeclarations) {
                exportsSelf || it.name in explicitlyNames
            }

            explicitlyExportedItems.filterIsInstance<ExportedModule>()
                .flatMap { it.importDeclarations }
                .flatMapTo(exportedDeclarations) {
                    getDeclarations(it)
                }
            exportedDeclarations
        }
    }

    /**
     * @return the [ValueDeclarationGroup] that this module exports,
     * both directly and through re-exported modules
     */
    val exportedValueDeclarationGroups: List<ValueDeclarationGroup>
        get() = getExportedDeclarations<ValueDeclarationGroup, ExportedValue.Psi>(
            cache.valueDeclarationGroups,
        ) { it.importedValueDeclarationGroups }

    /**
     * @return the [ForeignValueDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedForeignValueDeclarations: List<ForeignValueDecl>
        get() = getExportedDeclarations<ForeignValueDecl, ExportedValue.Psi>(
            cache.foreignValueDeclarations,
        ) { it.importedForeignValueDeclarations }

    /**
     * @return the [PSForeignDataDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedForeignDataDeclarations: List<PSForeignDataDeclaration>
        get() = getExportedDeclarations<PSForeignDataDeclaration, ExportedData.Psi>(
            cache.foreignDataDeclarations,
        ) { it.importedForeignDataDeclarations }

    /**
     * @return the [NewtypeDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedNewTypeDeclarations: List<NewtypeDecl>
        get() = getExportedDeclarations<NewtypeDecl, ExportedData.Psi>(
            cache.newTypeDeclarations,
        ) { it.importedNewTypeDeclarations }

    /**
     * @return the [NewtypeCtor] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedNewTypeConstructors: List<NewtypeCtor>
        get() {
            val explicitlyExportedItems = exports?.exportedItems
                ?: return cache.newTypeConstructors

            val exportedNewTypeConstructors =
                mutableListOf<NewtypeCtor>()

            for (exportedData in explicitlyExportedItems.filterIsInstance<ExportedData.Psi>()) {
                if (exportedData.exportsAll) {
                    exportedNewTypeConstructors.addIfNotNull(exportedData.newTypeDeclaration?.newTypeConstructor)
                } else {
                    exportedData.dataMembers
                        .mapNotNull { it.reference.resolve() }
                        .filterIsInstanceTo(exportedNewTypeConstructors)
                }
            }

            explicitlyExportedItems.filterIsInstance<ExportedModule>()
                .flatMap { it.importDeclarations }
                .flatMapTo(exportedNewTypeConstructors) { it.importedNewTypeConstructors }

            return exportedNewTypeConstructors
        }

    /**
     * @return the [DataDeclaration.Psi] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedDataDeclarations: List<DataDeclaration.Psi>
        get() = getExportedDeclarations<DataDeclaration.Psi, ExportedData.Psi>(
            cache.dataDeclarations,
        ) { it.importedDataDeclarations }

    /**
     * @return the [DataConstructor.Psi] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedDataConstructors: List<DataConstructor.Psi>
        get() {
            val explicitlyExportedItems = exports?.exportedItems
                ?: return cache.dataConstructors

            val exportedDataConstructors =
                mutableListOf<DataConstructor.Psi>()

            for (exportedData in explicitlyExportedItems.filterIsInstance<ExportedData.Psi>()) {
                if (exportedData.exportsAll) {
                    exportedData.dataDeclaration?.dataConstructors
                        ?.mapTo(exportedDataConstructors) { it }
                } else {
                    exportedData.dataMembers
                        .mapNotNull { it.reference.resolve() }
                        .filterIsInstanceTo(exportedDataConstructors)
                }
            }

            explicitlyExportedItems.filterIsInstance<ExportedModule>()
                .flatMap { it.importDeclarations }
                .flatMapTo(exportedDataConstructors) { it.importedDataConstructors }

            return exportedDataConstructors
        }

    /**
     * @return the [TypeDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedTypeSynonymDeclarations: List<TypeDecl>
        get() = getExportedDeclarations<TypeDecl, ExportedData.Psi>(
            cache.typeSynonymDeclarations,
        ) { it.importedTypeSynonymDeclarations }

    /**
     * @return the [ClassDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedClassDeclarations: List<ClassDecl>
        get() = getExportedDeclarations<ClassDecl, ExportedClass.Psi>(
            cache.classes,
        ) { it.importedClassDeclarations }

    /**
     * @return the [PSClassMember] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedClassMembers: List<PSClassMember>
        get() = getExportedDeclarations<PSClassMember, ExportedValue.Psi>(
            cache.classes
                .flatMap { it.classMembers.asSequence() }
                .toTypedArray(),
        ) { it.importedClassMembers }

    val reexportedModuleNames: List<String>
        get() =
            exports?.exportedItems
                ?.filterIsInstance(ExportedModule::class.java)
                ?.map { it.name }
                ?.toList()
                ?: emptyList()

    val exportedNames: List<String>
        get() =
            exports?.exportedItems
                ?.filter { it !is ExportedModule }
                ?.map { it.text.trim() }
                ?.toList()
                ?: emptyList()

    override val docComments: List<PsiComment>
        get() = getDocComments()

    fun addImportDeclaration(importDeclaration: ImportDeclaration) {
        val asPsi = project
            .service<PSPsiFactory>()
            .createImportDeclaration(importDeclaration)
        addImportDeclaration(asPsi)
    }

    fun addImportDeclaration(importDeclaration: Import) {
        val lastImportDeclaration = cache.imports.lastOrNull()
        val insertPosition = lastImportDeclaration ?: whereKeyword
        val newLine = project.service<PSPsiFactory>().createNewLine()
        addAfter(importDeclaration, insertPosition)
        addAfter(newLine, insertPosition)
        if (lastImportDeclaration == null) {
            addAfter(newLine, insertPosition)
        }
    }

    val exportsSelf: Boolean
        get() =
            exports?.exportedItems
                ?.filterIsInstance<ExportedModule>()
                ?.any { it.name == name }
                ?: true

}
