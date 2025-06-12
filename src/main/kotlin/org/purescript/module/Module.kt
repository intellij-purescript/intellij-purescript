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
import org.purescript.features.DocCommentOwner
import org.purescript.file.PSFile
import org.purescript.icons.PSIcons
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.inference.*
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.classes.ClassDecl
import org.purescript.module.declaration.classes.PSClassMember
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.fixity.ConstructorFixityDeclaration
import org.purescript.module.declaration.fixity.TypeFixityDeclaration
import org.purescript.module.declaration.fixity.ValueFixityDeclaration
import org.purescript.module.declaration.foreign.ForeignValueDecl
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.imports.Import
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.TypeDecl
import org.purescript.module.declaration.type.type.PSType
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.ValueOwner
import org.purescript.module.exports.*
import org.purescript.name.PSModuleName
import org.purescript.parser.FixityDeclType
import org.purescript.parser.WHERE
import org.purescript.psi.AStub
import org.purescript.psi.PSElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement

class Module : PsiNameIdentifierOwner, DocCommentOwner,
    PSStubbedElement<Module.Stub>, Importable, ValueOwner, Unifiable {
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
    val exportedTypeFixityDeclarations: Sequence<TypeFixityDeclaration>
        get() {
            val explicitlyExportedItems = cache.exportedItems
            return if (explicitlyExportedItems == null) {
                typeFixityDeclarations.asSequence()
            } else sequence {
                val explicitlyNames = explicitlyExportedItems
                    .filterIsInstance(ExportedTypeOperator.Psi::class.java)
                    .map { it.name }
                    .toSet()

                if (exportsSelf) {
                    yieldAll(typeFixityDeclarations.asSequence())
                } else {
                    yieldAll(typeFixityDeclarations.filter { it.name in explicitlyNames })
                }

                yieldAll(
                    explicitlyExportedItems
                        .asSequence()
                        .filterIsInstance<ExportedModule>()
                        .filter { it.name != name }
                        .flatMap { it.importDeclarations }
                        .flatMap { it.importedTypeFixityDeclarations }
                )
            }
        }
    val typeFixityDeclarations get() = children(TypeFixityDeclaration.Type)
    val constructorFixityDeclarations get() = children(ConstructorFixityDeclaration.Type)
    override val type: PSType? get() = null
    override fun addTypeDeclaration(variable: ValueDeclarationGroup): ValueDeclarationGroup {
        val factory = project.service<PSPsiFactory>()
        add(factory.createNewLines(2))
        return add(variable) as ValueDeclarationGroup
    }

    val imports: Array<Import> get() = cache.imports

    override val valueNames: Sequence<PsiNamedElement>
        get() = run {
            (valueGroups.asSequence() +
                    foreignValues.asSequence() +
                    classMembers.asSequence() +
                    imports.flatMap { it.importedValueNames }).toList()
        }.asSequence()
    override val constructors: List<PsiNamedElement>
        get() = cache.newTypeConstructors + cache.dataConstructors

    val exportedConstructors: Sequence<PsiNamedElement>
        get() = when (exports) {
            null -> constructors.asSequence()
            else -> exportedItems.flatMap { it.constructors }
        }
    val valueGroups get() = run { children<ValueDeclarationGroup>() }
    val foreignValues get() = run { children<ForeignValueDecl>() }
    val classes get() = run { children<ClassDecl>() }
    val classMembers get() = run { classes.flatMap { it.classMembers.toList() } }
    override fun unify() {}

    var cache: Cache = Cache()
    val exports get() = child<ExportList>()
    val exportedItems get() = exports?.exportedItems?.asSequence() ?: emptySequence()
    val valueFixityDeclarations get() = children(FixityDeclType)

    inner class Cache {
        val exportedItems by lazy { exports?.exportedItems }
        val classDeclarations by lazy { classes }
        val imports by lazy { children<Import>() }
        val importsByName by lazy { imports.groupBy { it.name } }
        val importsByAlias by lazy { imports.groupBy { it.importAlias?.name } }
        val importsByModule by lazy { imports.groupBy { it.moduleNameName } }
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
    override val docComments: List<PsiComment> get() = getDocComments()
    override fun getName(): String = greenStub?.name ?: nameIdentifier.name
    override fun setName(name: String): PsiElement? {
        val properName =
            project.service<PSPsiFactory>().createModuleName(name)
                ?: return null
        nameIdentifier.replace(properName)
        return this
    }

    /**
     * @return the [ValueFixityDeclaration] that this module exports,
     * both directly and through re-exported modules
     */
    val exportedValueFixityDeclarations: Sequence<ValueFixityDeclaration>
        get() {
            val explicitlyExportedItems = cache.exportedItems
            return if (explicitlyExportedItems == null) {
                valueFixityDeclarations.asSequence()
            } else sequence {
                val explicitlyNames = explicitlyExportedItems
                    .filterIsInstance(ExportedOperator.Psi::class.java)
                    .map { it.name }
                    .toSet()

                if (exportsSelf) {
                    yieldAll(valueFixityDeclarations.asSequence())
                } else {
                    yieldAll(valueFixityDeclarations.filter { it.name in explicitlyNames })
                }

                yieldAll(
                    explicitlyExportedItems
                        .asSequence()
                        .filterIsInstance<ExportedModule>()
                        .filter { it.name != name }
                        .flatMap { it.importDeclarations }
                        .flatMap { it.importedValueFixityDeclarations }
                )
            }
        }
    /**
     * @return the [ValueFixityDeclaration] that this module exports,
     * both directly and through re-exported modules
     */
    val exportedConstructorFixityDeclarations: Sequence<ConstructorFixityDeclaration>
        get() {
            val explicitlyExportedItems = cache.exportedItems
            return if (explicitlyExportedItems == null) {
                constructorFixityDeclarations.asSequence()
            } else sequence {
                val explicitlyNames = explicitlyExportedItems
                    .filterIsInstance(ExportedOperator.Psi::class.java)
                    .map { it.name }
                    .toSet()

                if (exportsSelf) {
                    yieldAll(constructorFixityDeclarations.asSequence())
                } else {
                    yieldAll(constructorFixityDeclarations.filter { it.name in explicitlyNames })
                }

                yieldAll(
                    explicitlyExportedItems
                        .asSequence()
                        .filterIsInstance<ExportedModule>()
                        .filter { it.name != name }
                        .flatMap { it.importDeclarations }
                        .flatMap { it.importedConstructorFixityDeclarations }
                )
            }
        }

    fun exportedFixityDeclarations(name: String): Sequence<ValueFixityDeclaration> {
        val explicitlyExportedItems = cache.exportedItems
        return if (explicitlyExportedItems == null) {
            valueFixityDeclarations.asSequence().filter { it.name == name }
        } else sequence {
            val explicitlyNames = explicitlyExportedItems
                .filterIsInstance(ExportedOperator.Psi::class.java)
                .map { it.name }
                .toSet()

            if (exportsSelf || name in explicitlyNames) {
                valueFixityDeclarations.firstOrNull { it.name == name }?.let { yield(it) }
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
        get() = run {
            getExportedDeclarations<PSForeignDataDeclaration, ExportedData.Psi>(
                cache.foreignDataDeclarations,
            ) { it.importedForeignDataDeclarations }
        }
    
    val exportedTypes get() = exportedDataDeclarations + exportedForeignDataDeclarations

    /**
     * @return the [NewtypeDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedNewTypeDeclarations: List<NewtypeDecl>
        get() = run {
            getExportedDeclarations<NewtypeDecl, ExportedData.Psi>(
                cache.newTypeDeclarations,
            ) { it.importedNewTypeDeclarations }
        }

    /**
     * @return the [DataDeclaration] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedDataDeclarations: List<PsiNamedElement>
        get() = run {
            getExportedDeclarations<PsiNamedElement, ExportedData.Psi>(
                cache.dataDeclarations.toList().toTypedArray()
            ) { it.importedDataDeclarations }
        }

    /**
     * @return the [TypeDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedTypeSynonymDeclarations: List<TypeDecl>
        get() = run {
            getExportedDeclarations<TypeDecl, ExportedData.Psi>(
                cache.typeSynonymDeclarations,
            ) { it.importedTypeSynonymDeclarations }
        }

    /**
     * @return the [ClassDecl] elements that this module exports,
     * both directly and through re-exported modules
     */
    val exportedClassDeclarations: List<ClassDecl>
        get() = run {
            getExportedDeclarations<ClassDecl, ExportedClass.Psi>(
                classes,
            ) { it.importedClassDeclarations }
        }

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

    fun addImportDeclaration(importDeclaration: ImportDeclaration) {
        if (importDeclaration.moduleName == name) return
        val imports = imports.filter {
            it.moduleNameName == importDeclaration.moduleName &&
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
        val lastImportDeclaration = imports.lastOrNull()
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

    fun exportedConstructorFixityDeclarations(name: String): Sequence<ConstructorFixityDeclaration> {
        val explicitlyExportedItems = cache.exportedItems
        return if (explicitlyExportedItems == null) {
            constructorFixityDeclarations.asSequence().filter { it.name == name }
        } else sequence {
            val explicitlyNames = explicitlyExportedItems
                .filterIsInstance(ExportedOperator.Psi::class.java)
                .map { it.name }
                .toSet()

            if (exportsSelf || name in explicitlyNames) {
                constructorFixityDeclarations.firstOrNull { it.name == name }?.let { yield(it) }
            }

            yieldAll(
                explicitlyExportedItems
                    .asSequence()
                    .filterIsInstance<ExportedModule>()
                    .filter { it.name != name }
                    .flatMap { it.importDeclarations }
                    .flatMap { it.importedConstructorFixityDeclarations(name) }
            )
        }
    }

    fun exportedTypeFixityDeclarations(name: String): Sequence<TypeFixityDeclaration>? {
        val explicitlyExportedItems = cache.exportedItems
        return if (explicitlyExportedItems == null) {
            typeFixityDeclarations.asSequence().filter { it.name == name }
        } else sequence {
            val explicitlyNames = explicitlyExportedItems
                .filterIsInstance(ExportedTypeOperator.Psi::class.java)
                .map { it.name }
                .toSet()

            if (exportsSelf || name in explicitlyNames) {
                typeFixityDeclarations.firstOrNull { it.name == name }?.let { yield(it) }
            }

            yieldAll(
                explicitlyExportedItems
                    .asSequence()
                    .filterIsInstance<ExportedModule>()
                    .filter { it.name != name }
                    .flatMap { it.importDeclarations }
                    .flatMap { it.importedTypeFixityDeclarations(name) }
            )
        }
    }
}
