package org.purescript

import com.intellij.psi.PsiFile
import com.intellij.psi.util.collectDescendantsOfType
import org.purescript.file.PSFile
import org.purescript.psi.PSForeignValueDeclaration
import org.purescript.psi.PSModule
import org.purescript.psi.newtype.PSNewTypeDeclarationImpl
import org.purescript.psi.PSValueDeclaration
import org.purescript.psi.classes.PSClassConstraint
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.exports.*
import org.purescript.psi.expression.PSExpressionConstructor
import org.purescript.psi.imports.*
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.typeconstructor.PSTypeConstructor
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration


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

fun PsiFile.getTypeConstructor(): PSTypeConstructor =
    collectDescendantsOfType<PSTypeConstructor>().single()

fun PsiFile.getTypeSynonymDeclarations(): Array<PSTypeSynonymDeclaration> =
    getModule().typeSynonymDeclarations

fun PsiFile.getTypeSynonymDeclaration(): PSTypeSynonymDeclaration =
    getTypeSynonymDeclarations().single()

fun PsiFile.getExpressionConstructors(): List<PSExpressionConstructor> =
    collectDescendantsOfType()

fun PsiFile.getExpressionConstructor(): PSExpressionConstructor =
    getExpressionConstructors().single()
