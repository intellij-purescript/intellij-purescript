package org.purescript.psi.exports

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.imports.PSImportDeclarationImpl
import org.purescript.psi.name.PSIdentifier
import org.purescript.psi.name.PSModuleName
import org.purescript.psi.name.PSProperName
import org.purescript.psi.name.PSSymbol
import org.purescript.psi.newtype.PSNewTypeDeclarationImpl

sealed class PSExportedItem(node: ASTNode) : PSPsiElement(node) {
    abstract override fun getName(): String
}

class PSExportedData(node: ASTNode) : PSExportedItem(node) {
    internal val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    val dataMemberList: PSExportedDataMemberList?
        get() = findChildByClass(PSExportedDataMemberList::class.java)

    val exportsAll: Boolean
        get() = dataMemberList?.doubleDot != null

    val dataMembers: Array<PSExportedDataMember>
        get() = dataMemberList?.dataMembers ?: emptyArray()

    val newTypeDeclaration: PSNewTypeDeclarationImpl?
        get() = reference.resolve() as? PSNewTypeDeclarationImpl

    val dataDeclaration: PSDataDeclaration?
        get() = reference.resolve() as? PSDataDeclaration

    override fun getName(): String = properName.name

    override fun getReference(): ExportedDataReference =
        ExportedDataReference(this)
}

class PSExportedClass(node: ASTNode) : PSExportedItem(node) {
    private val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSExportedKind(node: ASTNode) : PSExportedItem(node) {
    private val properName: PSProperName
        get() = findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSExportedOperator(node: ASTNode) : PSExportedItem(node) {
    val symbol: PSSymbol
        get() = findNotNullChildByClass(PSSymbol::class.java)

    override fun getName(): String = symbol.name
    override fun getReference() = ExportedOperatorReference(this)
}

class PSExportedType(node: ASTNode) : PSExportedItem(node) {
    private val identifier: PSIdentifier
        get() = findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name
}

class PSExportedModule(node: ASTNode) : PSExportedItem(node) {
    val moduleName: PSModuleName
        get() = findNotNullChildByClass(PSModuleName::class.java)

    val importDeclarations: Sequence<PSImportDeclarationImpl>
        get() = module
            ?.importDeclarations
            ?.asSequence()
            ?.filter { it.name == moduleName.name }
            ?: sequenceOf()

    override fun getName(): String = moduleName.name

    override fun getReference(): ExportedModuleReference {
        return ExportedModuleReference(this)
    }
}

class PSExportedValue(node: ASTNode) : PSExportedItem(node) {

    val identifier: PSIdentifier
        get() = findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName() = identifier.name

    override fun getReference(): ExportedValueReference {
        return ExportedValueReference(this)
    }
}
