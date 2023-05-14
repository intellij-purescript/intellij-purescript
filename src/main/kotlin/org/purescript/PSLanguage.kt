package org.purescript

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.util.PsiUtilCore.NULL_PSI_FILE
import org.purescript.ide.formatting.ImportDeclaration
import org.purescript.ide.formatting.ImportedClass
import org.purescript.module.declaration.Importable
import org.purescript.module.declaration.type.PSType

object PSLanguage : Language("Purescript", "text/purescript", "text/x-purescript", "application/x-purescript") {
    /**
     * These modules are built into the purescript compiler,
     * and have no corresponding source files.
     *
     * See [https://pursuit.purescript.org/builtins/docs/Prim] for details.
     */
    val BUILTIN_MODULES = setOf(
        "Prim",
        "Prim.Boolean",
        "Prim.Coerce",
        "Prim.Ordering",
        "Prim.Row",
        "Prim.RowList",
        "Prim.Symbol",
        "Prim.Int",
        "Prim.TypeError",
    )

    fun getBuiltins(project: Project, moduleName: String): List<PrimTypePsiElement> {
        val members = BUILTIN_MODULES_MAP[moduleName] ?: emptyList()
        return members.map { PrimTypePsiElement(project, moduleName, it) }
    }

    val BUILTIN_MODULES_MAP = mapOf<String, List<String>>(
        "Prim" to listOf(
            "Array",
            "Boolean",
            "Char",
            "Constraint",
            "Function", // TODO Function is really a kind, not a type
            "Int",
            "Number",
            "Partial", // TODO Partial is really a Class, not a type
            "Record", // TODO Record is really a kind, not a type
            "Row", // TODO Row is really a kind, not a type
            "String",
            "Symbol",
            "Type", // TODO Type is really a kind, not a typ
        ),
        "Prim.Boolean" to listOf(
            "True",
            "False",
        ),
        "Prim.Coerce" to listOf(
            "Coercible",
        ),
        "Prim.Ordering" to listOf(
            "Ordering",
            "LT",
            "EQ",
            "GT",
        ),
        "Prim.Row" to listOf(
            "Union",
            "Nub",
            "Lacks",
            "Cons",
        ),
        "Prim.RowList" to listOf(
            "RowList",
            "Cons",
            "Nil",
            "RowToList",
        ),
        "Prim.Symbol" to listOf(
            "Append",
            "Compare",
            "Cons",
        ),
        "Prim.Int" to listOf(
            "Add",
            "Compare",
            "Mul",
            "ToString",
        ),
        "Prim.TypeError" to listOf(
            "Warn",
            "Fail",
            "Doc",
            "Text",
            "Quote",
            "QuoteLabel",
            "Beside",
            "Above",
        ),
    )

    /**
     * These types are built into the purescript compiles,
     * and are always available.
     *
     * See [https://pursuit.purescript.org/builtins/docs/Prim] for details.
     */
    val BUILTIN_TYPES = setOf(
        "Array",
        "Boolean",
        "Char",
        "Constraint",
        "Function", // TODO Function is really a kind, not a type
        "Int",
        "Number",
        "Partial", // TODO Partial is really a Class, not a type
        "Record", // TODO Record is really a kind, not a type
        "Row", // TODO Row is really a kind, not a type
        "String",
        "Symbol",
        "Type", // TODO Type is really a kind, not a type
        // These should be imported from respective Prim modules, but quick fix is to always allow them
        // Prim.Boolean
        "True",
        "False",
        // Prim.Coerce
        "Coercible",
        // Prim.Ordering
        "Ordering",
        "LT",
        "EQ",
        "GT",
        // Prim.Row
        "Union",
        "Nub",
        "Lacks",
        "Cons",
        // Prim.RowList
        "RowList",
        "Cons",
        "Nil",
        "RowToList",
        // Prim.Symbol
        "Append",
        "Compare",
        "Cons",
        // Prim.Int
        "Add",
        "Compare",
        "Mul",
        "ToString",
        // Prim.TypeError
        "Warn",
        "Fail",
        "Doc",
        "Text",
        "Quote",
        "QuoteLabel",
        "Beside",
        "Above",
    )
}

class PrimTypePsiElement(private val project: Project, val moduleName: String, private val name: String) 
    : FakePsiElement(), Importable {
    override fun asImport() = ImportDeclaration(moduleName, importedItems = setOf(ImportedClass(name)))
    override val type: PSType? get() = null
    override fun getParent() = null
    override fun getName(): String = name
    override fun getProject(): Project = project
    override fun getContainingFile(): PsiFile = NULL_PSI_FILE
}
