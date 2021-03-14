package org.purescript

import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.psi.PSModule
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.imports.PSImportDeclarationImpl
import org.purescript.psi.imports.PSImportedItem


fun PsiFile.getModule(): PSModule {
    return (this as PSFile).module
}

fun PsiFile.getDataDeclaration(): PSDataDeclaration {
    return getModule().dataDeclarations.single()
}

fun PsiFile.getDataConstructor(): PSDataConstructor {
    return getModule().dataDeclarations.single().dataConstructorList!!.dataConstructors.single()
}

fun PsiFile.getExportedDataDeclarations(): List<PSDataDeclaration> {
    return getModule().exportedDataDeclarations
}

fun PsiFile.getImportDeclarations(): Array<PSImportDeclarationImpl> {
    return getModule().importDeclarations
}

fun PsiFile.getImportDeclaration(): PSImportDeclarationImpl {
    return getImportDeclarations().single()
}

fun PsiFile.getImportedItem(): PSImportedItem {
    return getImportDeclaration().importList!!.importedItems.single()
}
