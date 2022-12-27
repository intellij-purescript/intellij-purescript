package org.purescript.psi.declaration

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.stubs.IStubElementType
import org.purescript.psi.PSPsiFactory
import org.purescript.psi.PSStubbedElement
import org.purescript.psi.name.PSOperatorName

class PSFixityDeclaration : PSStubbedElement<PSFixityDeclarationStub>,
    PsiNameIdentifierOwner {

    constructor(node: ASTNode) : super(node)

    constructor(stub: PSFixityDeclarationStub, type: IStubElementType<*, *>)
        : super(stub, type)

    private val operatorName
        get() = findNotNullChildByClass(PSOperatorName::class.java)

    override fun getTextOffset(): Int = nameIdentifier.textOffset

    override fun getNameIdentifier() = operatorName

    override fun getName() = stub?.name ?: operatorName.name

    override fun setName(name: String): PsiElement? {
        val identifier = PSPsiFactory(project).createOperatorName(name)
            ?: return null
        nameIdentifier.replace(identifier)
        return this
    }
}