package org.purescript

import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.psi.PSForeignValueDeclaration
import org.purescript.psi.PSModule
import org.purescript.psi.PSNewTypeDeclarationImpl
import org.purescript.psi.PSValueDeclaration
import org.purescript.psi.classes.PSClassConstraint
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.exports.PSExportedData
import org.purescript.psi.exports.PSExportedDataMember
import org.purescript.psi.exports.PSExportedItem
import org.purescript.psi.imports.*


fun PsiFile.getModule(): PSModule =
    (this as PSFile).module

fun PsiFile.getDataDeclaration(): PSDataDeclaration =
    getModule().dataDeclarations.single()

fun PsiFile.getDataConstructor(): PSDataConstructor =
    getDataDeclaration().dataConstructorList!!.dataConstructors.single()

fun PsiFile.getExportedDataDeclarations(): List<PSDataDeclaration> =
    getModule().exportedDataDeclarations

fun PsiFile.getExportedClassDeclarations(): List<PSClassDeclaration> =
    getModule().exportedClassDeclarations

fun PsiFile.getImportDeclarations(): Array<PSImportDeclarationImpl> =
    getModule().importDeclarations

fun PsiFile.getImportDeclaration(): PSImportDeclarationImpl =
    getImportDeclarations().single()

fun PsiFile.getValueDeclarations(): Array<PSValueDeclaration> =
    getModule().valueDeclarations

fun PsiFile.getValueDeclaration(): PSValueDeclaration =
    getValueDeclarations().single()

fun PsiFile.getForeignValueDeclarations(): Array<PSForeignValueDeclaration> =
    getModule().foreignValueDeclarations

fun PsiFile.getForeignValueDeclaration(): PSForeignValueDeclaration =
    getForeignValueDeclarations().single()

fun PsiFile.getNewTypeDeclarations(): Array<PSNewTypeDeclarationImpl> =
    getModule().newTypeDeclarations

fun PsiFile.getNewTypeDeclaration(): PSNewTypeDeclarationImpl =
    getNewTypeDeclarations().single()

fun PsiFile.getImportedItem(): PSImportedItem =
    getImportDeclaration().importList!!.importedItems.single()

fun PsiFile.getImportedClass(): PSImportedClass =
    getImportedItem() as PSImportedClass

fun PsiFile.getImportedData(): PSImportedData =
    getImportedItem() as PSImportedData

fun PsiFile.getImportedValue(): PSImportedValue =
    getImportedItem() as PSImportedValue

fun PsiFile.getExportedItem(): PSExportedItem =
    getModule().exportList!!.exportedItems.single()

fun PsiFile.getExportedData(): PSExportedData =
    getExportedItem() as PSExportedData

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
