package org.purescript.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.ParserDefinition.SpaceRequirements
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.purescript.file.PSFile
import org.purescript.lexer.LayoutLexer
import org.purescript.lexer.PSLexer
import org.purescript.psi.PSElementType

class PSParserDefinition : ParserDefinition {
    override fun createLexer(project: Project): Lexer {
        return LayoutLexer(PSLexer())
    }

    override fun createParser(project: Project): PsiParser {
        return PureParser()
    }

    override fun getFileNodeType(): IFileElementType {
        return PSFile.Type
    }

    override fun getCommentTokens(): TokenSet {
        return TokenSet.create(DOC_COMMENT, MLCOMMENT, SLCOMMENT)
    }

    override fun getStringLiteralElements(): TokenSet {
        return kStrings
    }

    override fun createElement(node: ASTNode): PsiElement {
        val elementType = node.elementType
        if (elementType is PSElementType.HasPsi) return elementType.createPsi(node)
        return ASTWrapperPsiElement(node) // this should never happen
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile {
        return PSFile.Psi(viewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(
        left: ASTNode,
        right: ASTNode
    ): SpaceRequirements {
        return SpaceRequirements.MAY
    }
}
