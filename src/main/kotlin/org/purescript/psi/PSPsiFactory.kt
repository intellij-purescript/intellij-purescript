package org.purescript.psi

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.descendantsOfType
import org.intellij.lang.annotations.Language
import org.purescript.PSLanguage
import org.purescript.ide.formatting.*
import org.purescript.module.declaration.imports.*
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.expression.PSParens
import org.purescript.module.declaration.value.expression.controll.ifthenelse.PSIfThenElse
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.module.declaration.value.expression.namespace.PSExpressionWhere
import org.purescript.module.declaration.value.expression.namespace.PSLambda
import org.purescript.module.exports.ExportList
import org.purescript.module.declaration.value.expression.literals.RecordLabel
import org.purescript.name.PSIdentifier
import org.purescript.name.PSModuleName
import org.purescript.name.PSOperatorName


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

    fun createImportDeclarations(importDeclarations: ImportDeclarations): Pair<Import?, Import?> =
        createRangeFromText("module Foo where\n${importDeclarations.text}\n")

    fun createImportDeclaration(import: ImportDeclaration): Import =
        createFromText(
            buildString {
                appendLine("module Foo where")
                append(import.toString())
            }
        )!!

    fun createNewLine(): PsiElement = createNewLines()
    fun createSpace(): PsiElement = project.service<PsiParserFacade>().createWhiteSpaceFromText(" ")

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

    private inline fun <reified S : PsiElement, reified E : PsiElement> createRangeFromText(
        @Language("Purescript") code: String
    ): Pair<S?, E?> {
        val file = PsiFileFactory.getInstance(project)
            .createFileFromText(PSLanguage, code)
        return file.descendantsOfType<S>().firstOrNull() to
            file.descendantsOfType<E>().lastOrNull()
    }

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

    fun createExportList(vararg names: String): ExportList =
        createFromText(
            """
        |module Main (${names.joinToString(", ")}) where
    """.trimMargin()
        )!!

    fun createValueDeclaration(name: String, expr: String): ValueDecl? {
        return createFromText(
            """
            |module Main where
            |${name} = ${expr}
            """.trimMargin()
        )

    }

    fun createExpressionIdentifier(name: String): PSExpressionIdentifier? {
        return createFromText(
            """
            |module Main where
            |foo = ${name}
            """.trimMargin()
        )
    }

    fun createValueDeclarationGroup(
        name: String,
        expr: String
    ): ValueDeclarationGroup? {
        return createFromText(
            """
            |module Main where
            |${name} = ${expr}
            """.trimMargin()
        )
    }

    fun createIfThenElse(test: String, then:String, otherwise: String): PSIfThenElse? {
        return createFromText(
            """
            |module Main where
            |foo = if $test then $then else $otherwise
            """.trimMargin()
        )
    }

    fun createRecordLabel(text: String): RecordLabel? {
        return createFromText(
            """
            |module Main where
            |foo = {$text}
            """.trimMargin()
        )
    }

    fun createLambda(wholeExpression: String): PSLambda? {
        return createFromText(
            """
            |module Main where
            |foo = $wholeExpression
            """.trimMargin()
        )
    }

    fun createWhere(indentText: String, valueGroup: ValueDeclarationGroup): PSExpressionWhere? {
        return createFromText(
            """
            |module Main where
            |foo = x
            |${indentText}where 
            |${valueGroup.text.replaceIndent(indentText + "  ")}
            """.trimMargin()
        )
    }
}
