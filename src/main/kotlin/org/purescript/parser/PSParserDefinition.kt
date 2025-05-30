package org.purescript.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.purescript.file.PSFile
import org.purescript.lexer.PSLexer
import org.purescript.psi.PSElementType

class PSParserDefinition : ParserDefinition {
    override fun createLexer(project: Project) = PSLexer()
    override fun createParser(project: Project) = PureParser()
    override fun getFileNodeType(): IFileElementType = PSFile.Type
    override fun getStringLiteralElements(): TokenSet = kStrings
    override fun createFile(viewProvider: FileViewProvider) = PSFile(viewProvider)
    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) = SpaceRequirements.MAY
    override fun getCommentTokens(): TokenSet = TokenSet.create(DOC_COMMENT, MLCOMMENT, SLCOMMENT)
    override fun createElement(node: ASTNode): PsiElement {
        val elementType = node.elementType
        if (elementType is PSElementType.HasPsi<*>) return elementType.createPsi(node)
        return ASTWrapperPsiElement(node) // this should never happen
    }
}
