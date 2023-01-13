package org.purescript.psi

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import org.intellij.lang.annotations.Language
import org.purescript.PSLanguage
import org.purescript.ide.formatting.*
import org.purescript.psi.exports.ExportList
import org.purescript.psi.imports.*
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSOperatorName


/**
 * This should be com.intellij.psi.util.findDescendantOfType
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.findDescendantOfType(noinline predicate: (T) -> Boolean = { true }): T? {
    return findDescendantOfType({ true }, predicate)
}

/**
 * This should be com.intellij.psi.util.findDescendantOfType
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.findDescendantOfType(
    crossinline canGoInside: (PsiElement) -> Boolean,
    noinline predicate: (T) -> Boolean = { true }
): T? {
    var result: T? = null
    this.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            if (element is T && predicate(element)) {
                result = element
                stopWalking()
                return
            }

            if (canGoInside(element)) {
                super.visitElement(element)
            }
        }
    })
    return result
}

@Suppress("PSUnresolvedReference")
@Service
class PSPsiFactory(private val project: Project) {

    fun createModuleName(name: String): PSModuleName? =
        createFromText("module $name where")

    fun createImportDeclaration(importDeclaration: ImportDeclaration): Import.Psi? =
        createImportDeclaration(
            importDeclaration.moduleName,
            importDeclaration.hiding,
            importDeclaration.alias,
            importDeclaration
                .importedItems
                .sortedBy { it.name }
                .sortedBy {
                    when (it) {
                        is ImportedClass -> 1
                        is ImportedType -> 3
                        is ImportedData -> 4
                        is ImportedValue -> 5
                        is ImportedOperator -> 6
                    }
                }
                .map { importItemToString(it) }
        )

    private fun importItemToString(it: ImportedItem) = when (it) {
        is ImportedValue -> it.name
        is ImportedOperator -> "(${it.name})"
        is ImportedClass -> "class ${it.name}"
        is ImportedType -> "type (${it.name})"
        is ImportedData -> when {
            it.doubleDot -> "${it.name}(..)"
            it.dataMembers.isEmpty() -> it.name
            else -> "${it.name}(${it.dataMembers.sorted().joinToString()})"
        }
    }

    private fun createImportedItem(importedItem: ImportedItem): PSImportedItem? {
        return when (importedItem) {
            is ImportedClass -> createImportedClass(importedItem.name)
            is ImportedData -> createImportedData(
                importedItem.name,
                importedItem.doubleDot,
                importedItem.dataMembers
            )

            is ImportedOperator -> createImportedOperator(importedItem.name)

            is ImportedType -> createImportedType(importedItem.name)
            is ImportedValue -> createImportedValue(importedItem.name)
        }
    }

    fun createImportDeclaration(
        moduleName: String,
        hiding: Boolean = false,
        alias: String? = null,
        items: List<String>
    ): Import.Psi =
        createFromText(
            buildString {
                appendLine("module Foo where")
                append("import $moduleName")
                if (items.isNotEmpty()) {
                    if (hiding) {
                        append(" hiding")
                    }
                    append(
                        items.joinToString(
                            prefix = " (",
                            postfix = ")"
                        ) { it })
                }
                if (alias != null) {
                    append(" as $alias")
                }
            }
        )!!

    private fun createImportedClass(name: String): PSImportedClass? =
        createFromText(
            """
                module Foo where
                import Bar (class $name)
            """.trimIndent()
        )

    private fun createImportedOperator(name: String): PSImportedOperator? =
        createFromText(
            """
                module Foo where
                import Bar (($name))
            """.trimIndent()
        )

    private fun createImportedType(name: String): PSImportedType? =
        createFromText(
            """
                module Foo where
                import Bar (type ($name))
            """.trimIndent()
        )

    private fun createImportedData(
        name: String,
        doubleDot: Boolean = false,
        importedDataMembers: Collection<String> = emptyList()
    ): PSImportedData? =
        createFromText(
            buildString {
                appendLine("module Foo where")
                append("import Bar (")
                append(name)
                if (doubleDot) {
                    append("(..)")
                } else if (importedDataMembers.isNotEmpty()) {
                    append(
                        importedDataMembers.joinToString(
                            prefix = "(",
                            postfix = ")"
                        )
                    )
                }
                append(")")
            }
        )

    private fun createImportedValue(name: String): PSImportedValue? =
        createFromText(
            """
                module Foo where
                import Bar ($name)
            """.trimIndent()
        )

    fun createNewLine(): PsiElement = createNewLines()

    fun createNewLines(n: Int = 1): PsiElement =
        project.service<PsiParserFacade>()
            .createWhiteSpaceFromText("\n".repeat(n))

    private inline fun <reified T : PsiElement> createFromText(
        @Language(
            "Purescript"
        ) code: String
    ): T? =
        PsiFileFactory.getInstance(project)
            .createFileFromText(PSLanguage, code)
            .findDescendantOfType()

    fun createIdentifier(name: String): PSIdentifier? {
        return createFromText(
            """
            |module Main where
            |$name = 1
        """.trimMargin()
        )
    }

    fun createOperatorName(name: String): PSOperatorName? {
        return createFromText(
            """
            |module Main where
            |infixl 0 add as $name
        """.trimMargin()
        )
    }

    fun createParenthesis(around: String): PSParens? {
        return createFromText(
            """
            |module Main where
            |x = ($around)
        """.trimMargin()
        )
    }

    fun createExportList(vararg names: String): ExportList.Psi =
        createFromText(
            """
        |module Main (${names.joinToString(", ")}) where
    """.trimMargin()
        )!!
}
