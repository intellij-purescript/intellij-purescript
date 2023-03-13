package org.purescript

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.childrenOfType
import org.purescript.file.PSFile
import org.purescript.module.Module
import org.purescript.module.declaration.classes.ClassDecl
import org.purescript.module.declaration.classes.PSClassConstraint
import org.purescript.module.declaration.classes.PSClassMember
import org.purescript.module.declaration.data.DataConstructor
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.foreign.ForeignValueDecl
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.imports.*
import org.purescript.module.declaration.newtype.NewtypeCtor
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.TypeDecl
import org.purescript.module.declaration.type.typeconstructor.PSTypeConstructor
import org.purescript.module.declaration.value.ValueDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.module.declaration.value.binder.record.PunBinder
import org.purescript.module.declaration.value.expression.identifier.PSExpressionConstructor
import org.purescript.module.declaration.value.expression.identifier.PSExpressionIdentifier
import org.purescript.module.exports.*


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


fun PsiFile.getModule(): Module =
    (this as PSFile).module!!

fun PsiFile.getDataDeclaration(): DataDeclaration =
    getModule().cache.dataDeclarations.single()

fun PsiFile.getDataConstructor(): DataConstructor =
    getDataDeclaration().dataConstructorList!!.dataConstructors.single()

fun PsiFile.getExportedDataDeclarations(): List<DataDeclaration> =
    getModule().exportedDataDeclarations.filterIsInstance<DataDeclaration>()

fun PsiFile.getExportedClassDeclarations(): List<ClassDecl> =
    getModule().exportedClassDeclarations

fun PsiFile.getImportDeclarations(): Array<Import> =
    getModule().cache.imports

fun PsiFile.getImportAliases(): List<PSImportAlias> =
    getModule()
        .cache.imports
        .mapNotNull { it.importAlias }

fun PsiFile.getImportDeclaration(): Import =
    getImportDeclarations().single()

fun PsiFile.getImportAlias(): PSImportAlias =
    getImportAliases().single()

fun PsiFile.getValueDeclarationGroups(): List<ValueDeclarationGroup> =
    getModule().childrenOfType<ValueDeclarationGroup>()

fun PsiFile.getValueDeclarationGroup(): ValueDeclarationGroup =
    getValueDeclarationGroups().single()

fun PsiFile.getValueDeclarationGroupByName(name: String): ValueDeclarationGroup =
    getValueDeclarationGroups().single {it.name == name}

fun PsiFile.getValueDeclarations(): Array<ValueDecl> =
    getModule().cache.valueDeclarations

fun PsiFile.getValueDeclaration(): ValueDecl =
    getValueDeclarations().single()

fun PsiFile.getValueDeclarationByName(name: String): ValueDecl =
    getValueDeclarations().single { it.name == name}

fun PsiFile.getVarBinders(): List<VarBinder> =
    collectDescendantsOfType()

fun PsiFile.getVarBinder(): VarBinder =
    getVarBinders().single()

fun PsiFile.getPunBinders(): List<PunBinder> =
    collectDescendantsOfType()

fun PsiFile.getPunBinder(): PunBinder =
    getPunBinders().single()

fun PsiFile.getForeignValueDeclarations(): Array<ForeignValueDecl> =
    getModule().foreignValues

fun PsiFile.getForeignValueDeclaration(): ForeignValueDecl =
    getForeignValueDeclarations().single()

fun PsiFile.getNewTypeDeclarations(): Array<NewtypeDecl> =
    getModule().cache.newTypeDeclarations

fun PsiFile.getNewTypeDeclaration(): NewtypeDecl =
    getNewTypeDeclarations().single()

fun PsiFile.getNewTypeConstructor(): NewtypeCtor =
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

fun PsiFile.getExportedItems(): Array<ExportedItem<*>> {
    return getModule().exports!!.exportedItems
}

fun PsiFile.getExportedItem(): ExportedItem<*> =
    getExportedItems().single()

fun PsiFile.getExportedData(): ExportedData.Psi =
    getExportedItem() as ExportedData.Psi

fun PsiFile.getExportedValue(): ExportedValue.Psi =
    getExportedItem() as ExportedValue.Psi

fun PsiFile.getExportedModule(): ExportedModule =
    getExportedItem() as ExportedModule

fun PsiFile.getExportedDataMember(): PSExportedDataMember =
    getExportedData().dataMemberList!!.dataMembers.single()

fun PsiFile.getClassDeclarations(): Array<ClassDecl> =
    getModule().classes

fun PsiFile.getClassDeclaration(): ClassDecl =
    getClassDeclarations().single()

fun PsiFile.getClassMember(): PSClassMember =
    getClassDeclaration().classMembers.single()

fun PsiFile.getClassConstraint(): PSClassConstraint =
    getClassDeclaration().classConstraints.single()

fun PsiFile.getTypeConstructors(): List<PSTypeConstructor> =
    collectDescendantsOfType()

fun PsiFile.getTypeConstructor(): PSTypeConstructor =
    getTypeConstructors().single()

fun PsiFile.getTypeSynonymDeclarations(): Array<TypeDecl> =
    getModule().cache.typeSynonymDeclarations

fun PsiFile.getTypeSynonymDeclaration(): TypeDecl =
    getTypeSynonymDeclarations().single()

fun PsiFile.getForeignDataDeclarations(): Array<PSForeignDataDeclaration> =
    getModule().cache.foreignDataDeclarations

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
