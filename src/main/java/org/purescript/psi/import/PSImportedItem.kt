package org.purescript.psi.import

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import org.purescript.psi.PSIdentifier
import org.purescript.psi.PSProperName
import org.purescript.psi.PSPsiElement

sealed class PSImportedItem(node: ASTNode) : PSPsiElement(node) {
    abstract override fun getName(): String

    internal val importDeclaration: PSImportDeclarationImpl?
        get() =
            PsiTreeUtil.getParentOfType(this, PSImportDeclarationImpl::class.java)
}

class PSImportedClass(node: ASTNode) : PSImportedItem(node) {
    private val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSImportedData(node: ASTNode) : PSImportedItem(node) {
    internal val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name

    override fun getReference(): ImportedDataReference = ImportedDataReference(this)
}

class PSImportedKind(node: ASTNode) : PSImportedItem(node) {
    private val properName: PSProperName
        get() =
            findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSImportedOperator(node: ASTNode) : PSImportedItem(node) {
    private val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name
}

class PSImportedType(node: ASTNode) : PSImportedItem(node) {
    private val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name
}

class PSImportedValue(node: ASTNode) : PSImportedItem(node) {
    val identifier: PSIdentifier
        get() =
            findNotNullChildByClass(PSIdentifier::class.java)

    override fun getName(): String = identifier.name

    override fun getReference(): ImportedValueReference =
        ImportedValueReference(this)
}

