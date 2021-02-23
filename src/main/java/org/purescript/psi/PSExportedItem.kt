package org.purescript.psi

import com.intellij.lang.ASTNode
import org.purescript.file.PSFile
import org.purescript.psi.`var`.ExportedValueReference

sealed class PSExportedItem(node: ASTNode) : PSPsiElement(node) {
    val module: PSModule get() = (containingFile as PSFile).module
}

class PSExportedData(node: ASTNode) : PSExportedItem(node) {
    val dataMemberList: PSExportedDataMemberList? = findChildByClass(PSExportedDataMemberList::class.java)
}

class PSExportedClass(node: ASTNode) : PSExportedItem(node)
class PSExportedKind(node: ASTNode) : PSExportedItem(node)
class PSExportedModule(node: ASTNode) : PSExportedItem(node)
class PSExportedOperator(node: ASTNode) : PSExportedItem(node)
class PSExportedType(node: ASTNode) : PSExportedItem(node)

class PSExportedValue(node: ASTNode) : PSExportedItem(node) {

    val identifier = findNotNullChildByClass(PSIdentifierImpl::class.java)

    override fun getName() = identifier.name

    override fun getReference(): ExportedValueReference {
        return ExportedValueReference(this)
    }
}
