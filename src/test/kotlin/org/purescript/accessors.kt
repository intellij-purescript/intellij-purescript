package org.purescript

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import org.purescript.file.PSFile
import org.purescript.psi.PSForeignDataDeclaration
import org.purescript.psi.PSForeignValueDeclaration
import org.purescript.psi.PSModule
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.classes.PSClassConstraint
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.exports.*
import org.purescript.psi.expression.PSExpressionConstructor
import org.purescript.psi.expression.PSExpressionIdentifier
import org.purescript.psi.imports.*
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclaration
import org.purescript.psi.typeconstructor.PSTypeConstructor
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration


/**
 * This should be com.intellij.psi.util.forEachDescendantOfType
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.forEachDescendantOfType(
    crossinline canGoInside: (PsiElement) -> Boolean,
    noinline action: (T) -> Unit
) {
    this.accept(object : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            if (canGoInside(element)) {
                super.visitElement(element)
            }

            if (element is T) {
                action(element)
            }
        }
    })
}


/**
 * This should be com.intellij.psi.util.collectDescendantsOfType
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.collectDescendantsOfType(noinline predicate: (T) -> Boolean = { true }): List<T> {
    return collectDescendantsOfType({ true }, predicate)
}

/**
 * This should be com.intellij.psi.util.collectDescendantsOfType
 * but is currently missing from the EAP build
 *
 * Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 */
inline fun <reified T : PsiElement> PsiElement.collectDescendantsOfType(
    crossinline canGoInside: (PsiElement) -> Boolean,
    noinline predicate: (T) -> Boolean = { true }
): List<T> {
    val result = ArrayList<T>()
    forEachDescendantOfType<T>(canGoInside) {
        if (predicate(it)) {
            result.add(it)
        }
    }
    return result
}


fun PsiFile.getModule(): PSModule =
    (this as PSFile).module!!

fun PsiFile.getDataDeclaration(): PSDataDeclaration =
    getModule().dataDeclarations.single()

fun PsiFile.getDataConstructor(): PSDataConstructor =
    getDataDeclaration().dataConstructorList!!.dataConstructors.single()

fun PsiFile.getExportedDataDeclarations(): List<PSDataDeclaration> =
    getModule().exportedDataDeclarations

fun PsiFile.getExportedClassDeclarations(): List<PSClassDeclaration> =
    getModule().exportedClassDeclarations

fun PsiFile.getImportDeclarations(): Array<PSImportDeclaration> =
    getModule().importDeclarations
fun PsiFile.getImportAliases(): List<PSImportAlias> =
    getModule()
        .importDeclarations
        .mapNotNull { it.importAlias }

fun PsiFile.getImportDeclaration(): PSImportDeclaration =
    getImportDeclarations().single()
fun PsiFile.getImportAlias(): PSImportAlias =
    getImportAliases().single()

fun PsiFile.getValueDeclarations(): Array<PSValueDeclaration> =
    getModule().valueDeclarations

fun PsiFile.getValueDeclaration(): PSValueDeclaration =
    getValueDeclarations().single()

fun PsiFile.getVarBinders(): List<PSVarBinder> =
    collectDescendantsOfType()

fun PsiFile.getVarBinder(): PSVarBinder =
    getVarBinders().single()

fun PsiFile.getForeignValueDeclarations(): Array<PSForeignValueDeclaration> =
    getModule().foreignValueDeclarations

fun PsiFile.getForeignValueDeclaration(): PSForeignValueDeclaration =
    getForeignValueDeclarations().single()

fun PsiFile.getNewTypeDeclarations(): Array<PSNewTypeDeclaration> =
    getModule().newTypeDeclarations

fun PsiFile.getNewTypeDeclaration(): PSNewTypeDeclaration =
    getNewTypeDeclarations().single()

fun PsiFile.getNewTypeConstructor(): PSNewTypeConstructor =
    getNewTypeDeclaration().newTypeConstructor

fun PsiFile.getImportedItem(): PSImportedItem =
    getImportDeclaration().importList!!.importedItems.single()

fun PsiFile.getImportedClass(): PSImportedClass =
    getImportedItem() as PSImportedClass

fun PsiFile.getImportedData(): PSImportedData =
    getImportedItem() as PSImportedData

fun PsiFile.getImportedValue(): PSImportedValue =
    getImportedItem() as PSImportedValue

fun PsiFile.getImportedOperator(): PSImportedOperator =
    getImportedItem() as PSImportedOperator

fun PsiFile.getExportedItems(): Array<PSExportedItem> =
    getModule().exportList!!.exportedItems

fun PsiFile.getExportedItem(): PSExportedItem =
    getExportedItems().single()

fun PsiFile.getExportedData(): PSExportedData =
    getExportedItem() as PSExportedData

fun PsiFile.getExportedValue(): PSExportedValue =
    getExportedItem() as PSExportedValue

fun PsiFile.getExportedModule(): PSExportedModule =
    getExportedItem() as PSExportedModule

fun PsiFile.getExportedDataMember(): PSExportedDataMember =
    getExportedData().dataMemberList!!.dataMembers.single()

fun PsiFile.getClassDeclarations(): Array<PSClassDeclaration> =
    getModule().classDeclarations

fun PsiFile.getClassDeclaration(): PSClassDeclaration =
    getClassDeclarations().single()

fun PsiFile.getClassMember(): PSClassMember =
    getClassDeclaration().classMembers.single()

fun PsiFile.getClassConstraint(): PSClassConstraint =
    getClassDeclaration().classConstraints.single()

fun PsiFile.getTypeConstructors(): List<PSTypeConstructor> =
    collectDescendantsOfType()

fun PsiFile.getTypeConstructor(): PSTypeConstructor =
    getTypeConstructors().single()

fun PsiFile.getTypeSynonymDeclarations(): Array<PSTypeSynonymDeclaration> =
    getModule().typeSynonymDeclarations

fun PsiFile.getTypeSynonymDeclaration(): PSTypeSynonymDeclaration =
    getTypeSynonymDeclarations().single()

fun PsiFile.getForeignDataDeclarations(): Array<PSForeignDataDeclaration> =
    getModule().foreignDataDeclarations

fun PsiFile.getForeignDataDeclaration(): PSForeignDataDeclaration =
    getForeignDataDeclarations().single()

fun PsiFile.getExpressionConstructors(): List<PSExpressionConstructor> =
    collectDescendantsOfType()

fun PsiFile.getExpressionConstructor(): PSExpressionConstructor =
    getExpressionConstructors().single()

fun PsiFile.getExpressionIdentifiers(): List<PSExpressionIdentifier> =
    collectDescendantsOfType()

fun PsiFile.getExpressionIdentifier(): PSExpressionIdentifier =
    getExpressionIdentifiers().single()
