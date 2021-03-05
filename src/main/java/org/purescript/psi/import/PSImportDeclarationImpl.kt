package org.purescript.psi.import

import com.intellij.lang.ASTNode
import org.purescript.psi.*

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
    val importName get() = findChildByClass(PSProperName::class.java)

    /**
     * The import list of this import declaration.
     * It being null implies that all items are imported.
     */
    val importList: PSImportList?
        get() =
            findChildByClass(PSImportList::class.java)

    /**
     * The import alias of this import declaration,
     * if it has one.
     */
    val importAlias: PSImportAlias?
        get() =
            findChildByClass(PSImportAlias::class.java)

    override fun getName() = importName?.name

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
     * Returns a reference to the [PSModule] that this declaration is
     * importing from
     */
    override fun getReference(): ModuleReference =
        ModuleReference(this)

    /**
     * The [PSModule] that this declaration is importing from
     */
    val importedModule get(): PSModule? = reference.resolve()

    /**
     * All [PSValueDeclaration] elements imported by this declaration
     */
    val importedValueDeclarations
        get(): Sequence<PSValueDeclaration> =
            importedModule?.let { importedModule ->
                when {
                    isHiding -> {
                        importedModule
                            .exportedValueDeclarations
                            .filter { it.name !in namedImports.toSet() }
                            .asSequence()
                    }
                    namedImports.isNotEmpty() -> {
                        importedModule
                            .exportedValueDeclarations
                            .filter { it.name in namedImports.toSet() }
                            .asSequence()
                    }
                    else -> {
                        importedModule.exportedValueDeclarations.asSequence()
                    }
                }
            } ?: sequenceOf()

    /**
     * All [PSForeignValueDeclaration] elements imported by this declaration
     */
    val importedForeignValueDeclarations: List<PSForeignValueDeclaration>
        get() {
            val exportedForeignValueDeclarations = importedModule?.exportedForeignValueDeclarations
                ?: return emptyList()

            val importedItems = importList?.importedItems
                ?: return exportedForeignValueDeclarations

            val importedValueNames = importedItems.filterIsInstance<PSImportedValue>()
                .map { it.name }
                .toSet()

            return if (isHiding) {
                exportedForeignValueDeclarations.filter { it.name !in importedValueNames }
            } else {
                exportedForeignValueDeclarations.filter { it.name in importedValueNames }
            }
        }
}
