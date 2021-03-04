package org.purescript.psi.import

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.purescript.psi.*

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

    override fun getReference(): PsiReference {
        return ModuleReference(this)
    }

    val importedModule get(): PSModule? = ModuleReference(this).resolve()

    val importedValues
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
}
