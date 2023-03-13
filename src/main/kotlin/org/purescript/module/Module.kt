package org.purescript.module

import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.components.service
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.stubs.*
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager.getCachedValue
import com.intellij.util.containers.addIfNotNull
import org.purescript.features.DocCommentOwner
import org.purescript.icons.PSIcons
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.classes.ClassDecl
import org.purescript.module.declaration.classes.PSClassMember
import org.purescript.module.declaration.data.DataConstructor
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.module.declaration.foreign.ForeignValueDecl
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.newtype.NewtypeCtor
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.PSType
import org.purescript.module.declaration.type.TypeDecl
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueNamespace
import org.purescript.module.exports.*
import org.purescript.name.PSModuleName
import org.purescript.parser.FixityDeclType
import org.purescript.parser.WHERE
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement

class Module : PsiNameIdentifierOwner, DocCommentOwner,
    PSStubbedElement<Module.Stub>, Importable, ValueNamespace {
    object Type : PSElementType.WithPsiAndStub<Stub, Module>("Module") {
        override fun createPsi(node: ASTNode) = Module(node)
        override fun createPsi(stub: Stub) = Module(stub, this)
        override fun createStub(psi: Module, p: StubElement<*>) = Stub(psi.name, p)
        override fun serialize(stub: Stub, data: StubOutputStream) = data.writeName(stub.name)
        override fun deserialize(d: StubInputStream, p: StubElement<*>?) = Stub(d.readNameString()!!, p)
        override fun indexStub(stub: Stub, sink: IndexSink) = sink.occurrence(ModuleNameIndex.KEY, stub.name)
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
    override val type: PSType? get() = null
    override val valueNames: Sequence<PsiNamedElement>
        get() = valueGroups.asSequence() + foreignValues.asSequence() + classMembers.asSequence()
    val valueGroups get() = getCachedValue(this) { create(children<ValueDeclarationGroup>(), this) }
    val foreignValues get() = getCachedValue(this) { create(children<ForeignValueDecl>(), this) }
    val classes get() = getCachedValue(this) { create(children<ClassDecl>(), this) }
    val classMembers get() = getCachedValue(this) { create(classes.flatMap { it.classMembers.toList() }, this) }

    // TODO clean up this name
    override fun toString(): String = "PSModule($elementType)"
    var cache: Module.Cache = Cache()
    val exports get() = child<ExportList>()
    val fixityDeclarations get() = children(FixityDeclType)

    inner class Cache {
        val exportedItems by lazy { exports?.exportedItems }
        val classDeclarations by lazy { classes }
        val imports by lazy { children<Import>() }
        val importsByName by lazy { imports.groupBy { it.name } }
        val importsByAlias by lazy { imports.groupBy { it.importAlias?.name } }
        val importsByModule by lazy { imports.groupBy { it.moduleName.name } }
        val valueDeclarations: Array<ValueDecl> by lazy {
            valueGroups
                .flatMap { it.valueDeclarations.asSequence() }
                .toTypedArray()
        }
        val dataDeclarations by lazy { children<DataDeclaration>() }
        val dataConstructors by lazy { dataDeclarations.flatMap { it.dataConstructors.toList() } }
        val newTypeDeclarations by lazy { children<NewtypeDecl>() }
        val newTypeConstructors by lazy { newTypeDeclarations.map { it.newTypeConstructor } }
        val typeSynonymDeclarations by lazy { children<TypeDecl>() }
        val foreignDataDeclarations by lazy { children<PSForeignDataDeclaration>() }
    }

    override fun subtreeChanged() {
        cache = Cache()
        super.subtreeChanged()
    }

    override fun getNameIdentifier() = findNotNullChildByClass(PSModuleName::class.java)
    override fun getTextOffset() = nameIdentifier.textOffset
    override fun getName(): String = greenStub?.name ?: nameIdentifier.name
    override fun setName(name: String): PsiElement? {
        val properName =
            project.service<PSPsiFactory>().createModuleName(name)
                ?: return null
        nameIdentifier.replace(properName)
        return this
    }

    /**
     * @return the [FixityDeclaration] that this module exports,
     * both directly and through re-exported modules
     */
    val exportedFixityDeclarations: Sequence<FixityDeclaration>
        get() {
            val explicitlyExportedItems = cache.exportedItems
            return if (explicitlyExportedItems == null) {
                fixityDeclarations.asSequence()
            } else sequence {
                val explicitlyNames = explicitlyExportedItems
                    .filterIsInstance(ExportedOperator.Psi::class.java)
                    .map { it.name }
                    .toSet()

                val exportsSelf = explicitlyExportedItems
                    .filterIsInstance<ExportedModule>()
                    .any { it.name == name }

                if (exportsSelf) {
                    yieldAll(fixityDeclarations.asSequence())
                } else {
                    yieldAll(fixityDeclarations.filter { it.name in explicitlyNames })
                }

                yieldAll(
                    explicitlyExportedItems
                        .asSequence()
                        .filterIsInstance<ExportedModule>()
                        .filter { it.name != name }
                        .flatMap { it.importDeclarations }
                        .flatMap { it.importedFixityDeclarations }
                )
            }
        }

    fun exportedFixityDeclarations(name: String): Sequence<FixityDeclaration> {
        val explicitlyExportedItems = cache.exportedItems
        return if (explicitlyExportedItems == null) {
            fixityDeclarations.asSequence().filter { it.name == name }
        } else sequence {
            val explicitlyNames = explicitlyExportedItems
                .filterIsInstance(ExportedOperator.Psi::class.java)
                .map { it.name }
                .toSet()

            val exportsSelf = explicitlyExportedItems.filterIsInstance<ExportedModule>().any { it.name == name }

            if (exportsSelf || name in explicitlyNames) {
                fixityDeclarations.first { it.name == name }.let { yield(it) }
            }

            yieldAll(
                explicitlyExportedItems
                    .asSequence()
                    .filterIsInstance<ExportedModule>()
                    .filter { it.name != name }
                    .flatMap { it.importDeclarations }
                    .flatMap { it.importedFixityDeclarations(name) }
            )
        }
    }

    /**
     * @return the where keyword in the module header
     */
    val whereKeyword: PsiElement get() = findNotNullChildByType(WHERE)

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
        val explicitlyExportedItems = cache.exportedItems
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
            if (exportsSelf) {
                exportedDeclarations.addAll(declarations)
            } else {
                declarations.filterTo(exportedDeclarations) { it.name in explicitlyNames }
            }

            explicitlyExportedItems.filterIsInstance<ExportedModule>()
                .filter { it.name != name }
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
        get() {
            val explicitlyExportedItems = cache.exportedItems
            return if (explicitlyExportedItems == null) {
                valueGroups.toList()
            } else {
                val explicitlyNames = explicitlyExportedItems
                    .filterIsInstance(ExportedValue.Psi::class.java)
                    .map { it.name }
                    .toSet()
                val exportedModules = explicitlyExportedItems.filterIsInstance<ExportedModule>().toList()

                val exportsSelf = exportedModules.any { it.name == name }
                val local = if (exportsSelf) {
                    valueGroups.toList()
                } else {
                    valueGroups.filter { it.name in explicitlyNames }
                }
                val fromImports = exportedModules
                    .filter { it.name != name }
                    .flatMap { it.importDeclarations }
                    .flatMap { it.importedValueDeclarationGroups }
                (local + fromImports).toList()
            }
        }

    val exportedValueNames: List<PsiNamedElement>
        get() = exportedForeignValueDeclarations +
                exportedValueDeclarationGroups +
                exportedClassDeclarations.flatMap { it.classMembers.asSequence() }
    /**
     * @return the [ForeignValueDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedForeignValueDeclarations: List<ForeignValueDecl>
        get() = getExportedDeclarations<ForeignValueDecl, ExportedValue.Psi>(foreignValues)
        { it.importedForeignValueDeclarations }

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
     * @return the [DataDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedDataDeclarations: List<DataDeclaration>
        get() = getExportedDeclarations<DataDeclaration, ExportedData.Psi>(
            cache.dataDeclarations,
        ) { it.importedDataDeclarations }

    /**
     * @return the [DataConstructor] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedDataConstructors: List<DataConstructor>
        get() {
            val explicitlyExportedItems = exports?.exportedItems
                ?: return cache.dataConstructors

            val exportedDataConstructors =
                mutableListOf<DataConstructor>()

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
            classes,
        ) { it.importedClassDeclarations }

    /**
     * @return the [PSClassMember] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedClassMembers: List<PSClassMember>
        get() = getExportedDeclarations<PSClassMember, ExportedValue.Psi>(classMembers.toTypedArray()) { it.importedClassMembers }

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
        if (importDeclaration.moduleName == name) return
        val imports = cache.imports.filter {
            it.moduleName.name == importDeclaration.moduleName &&
                    it.importAlias?.name == importDeclaration.alias
        }
        if (imports.any {
                ImportDeclaration.fromPsiElement(it).run {
                    !hiding && (importedItems.isEmpty() || importedItems.containsAll(importDeclaration.importedItems))
                }
            }) return // already imported
        val oldImport = imports.firstOrNull { !it.isHiding }
        if (oldImport != null) {
            val fromPsiElement = ImportDeclaration.fromPsiElement(oldImport)
            val importedItems = fromPsiElement.importedItems + importDeclaration.importedItems
            val mergedImport = fromPsiElement.withItems(*importedItems.toTypedArray())
            val asPsi = project
                .service<PSPsiFactory>()
                .createImportDeclaration(mergedImport)
            oldImport.replace(asPsi)
        } else {
            val asPsi = project
                .service<PSPsiFactory>()
                .createImportDeclaration(importDeclaration)
            addImportDeclaration(asPsi)
        }
    }

    private fun addImportDeclaration(importDeclaration: Import) {
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

    override fun getPresentation() = object : ItemPresentation {
        override fun getPresentableText() = name
        override fun getIcon(unused: Boolean) = PSIcons.FILE
        override fun getLocationString() = when (val projectPath = project.guessProjectDir()) {
            null -> containingFile.virtualFile.path
            else -> projectPath.toNioPath().relativize(containingFile.virtualFile.toNioPath()).toString()
        }
    }

    override fun getIcon(flags: Int) = PSIcons.FILE
    fun exportedValue(name: String): Sequence<Importable> {
        val exportedItems =
            cache.exportedItems ?: return valueNames.filterIsInstance<Importable>().filter { it.name == name }
        return when {
            exportedItems.any { it.name == name } -> valueNames.filterIsInstance<Importable>()
                .filter { it.name == name }

            else -> sequence {
                exportedItems.filterIsInstance<ExportedModule>()
                    .flatMap {
                        if (it.name == this@Module.name) {
                            this@Module.valueNames.filterIsInstance<Importable>().filter { it.name == name }
                        } else {
                            it.importDeclarations.flatMap { it.importedValue(name) }
                        }
                    }.let { yieldAll(it) }
            }
        }
    }
}
