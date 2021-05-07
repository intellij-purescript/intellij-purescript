package org.purescript.features

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.PSLexer
import org.purescript.parser.PSTokens.Companion.ARROW
import org.purescript.parser.PSTokens.Companion.BACKSLASH
import org.purescript.parser.PSTokens.Companion.CHAR
import org.purescript.parser.PSTokens.Companion.COMMA
import org.purescript.parser.PSTokens.Companion.DARROW
import org.purescript.parser.PSTokens.Companion.DCOLON
import org.purescript.parser.PSTokens.Companion.DDOT
import org.purescript.parser.PSTokens.Companion.DOC_COMMENT
import org.purescript.parser.PSTokens.Companion.DOT
import org.purescript.parser.PSTokens.Companion.EQ
import org.purescript.parser.PSTokens.Companion.FLOAT
import org.purescript.parser.PSTokens.Companion.IDENT
import org.purescript.parser.PSTokens.Companion.LARROW
import org.purescript.parser.PSTokens.Companion.LDARROW
import org.purescript.parser.PSTokens.Companion.MLCOMMENT
import org.purescript.parser.PSTokens.Companion.MODULE_PREFIX
import org.purescript.parser.PSTokens.Companion.NATURAL
import org.purescript.parser.PSTokens.Companion.OPERATOR
import org.purescript.parser.PSTokens.Companion.OPTIMISTIC
import org.purescript.parser.PSTokens.Companion.PIPE
import org.purescript.parser.PSTokens.Companion.PROPER_NAME
import org.purescript.parser.PSTokens.Companion.SEMI
import org.purescript.parser.PSTokens.Companion.SLCOMMENT
import org.purescript.parser.PSTokens.Companion.STRING
import org.purescript.parser.PSTokens.Companion.STRING_ERROR
import org.purescript.parser.PSTokens.Companion.STRING_ESCAPED
import org.purescript.parser.PSTokens.Companion.STRING_GAP
import org.purescript.psi.PSForeignDataDeclaration
import org.purescript.psi.PSForeignValueDeclaration
import org.purescript.psi.PSModule
import org.purescript.psi.PSVarBinderImpl
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.PSFixityDeclaration
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.imports.PSImportAlias
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclarationImpl
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is PSValueDeclaration
            || psiElement is PSVarBinderImpl
            || psiElement is PSModule
            || psiElement is PSForeignValueDeclaration
            || psiElement is PSNewTypeDeclarationImpl
            || psiElement is PSNewTypeConstructor
            || psiElement is PSImportAlias
            || psiElement is PSDataDeclaration
            || psiElement is PSDataConstructor
            || psiElement is PSClassDeclaration
            || psiElement is PSTypeSynonymDeclaration
            || psiElement is PSClassMember
            || psiElement is PSFixityDeclaration
            || psiElement is PSForeignDataDeclaration

    override fun getWordsScanner(): WordsScanner {
        val psWordScanner = DefaultWordsScanner(
            PSLexer(),
            TokenSet.create(IDENT, PROPER_NAME, MODULE_PREFIX),
            TokenSet.create(MLCOMMENT, SLCOMMENT, DOC_COMMENT),
            TokenSet.create(
                STRING,
                STRING_ERROR,
                STRING_ESCAPED,
                STRING_GAP,
                CHAR,
                NATURAL,
                FLOAT
            ),
            TokenSet.EMPTY,
            TokenSet.create(
                OPERATOR,
                ARROW,
                BACKSLASH, // maybe?!
                COMMA,
                DARROW,
                DCOLON,
                DDOT,
                DOT,
                EQ, // maybe?!
                LARROW,
                LDARROW,
                OPTIMISTIC,
                PIPE,
                SEMI,
            )
        )
        return psWordScanner
    }

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return when (element) {
            is PSValueDeclaration -> "value"
            is PSVarBinderImpl -> "parameter"
            is PSModule -> "module"
            is PSNewTypeDeclarationImpl -> "newtype"
            is PSNewTypeConstructor -> "newtype constructor"
            is PSImportAlias -> "import alias"
            is PSDataDeclaration -> "data"
            is PSDataConstructor -> "data constructor"
            is PSClassDeclaration -> "class"
            is PSForeignValueDeclaration -> "foreign value"
            is PSForeignDataDeclaration -> "foreign data"
            is PSTypeSynonymDeclaration -> "type synonym"
            is PSClassMember -> "class member"
            is PSFixityDeclaration -> "operator"
            else -> "unknown"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        when (element) {
            is PSValueDeclaration -> {
                return "${element.module?.name}.${element.name}"
            }
            is PsiNamedElement -> {
                val name = element.name
                if (name != null) {
                    return name
                }
            }
        }
        return ""
    }

    override fun getNodeText(
        element: PsiElement,
        useFullName: Boolean
    ): String {
        if (useFullName) {
            return getDescriptiveName(element)
        } else if (element is PsiNamedElement) {
            val name = element.name
            if (name != null) {
                return name
            }
        }
        return ""
    }
}
