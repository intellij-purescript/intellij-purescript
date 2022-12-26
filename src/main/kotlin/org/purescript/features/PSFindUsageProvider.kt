package org.purescript.features

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.PSLexer
import org.purescript.parser.ARROW
import org.purescript.parser.BACKSLASH
import org.purescript.parser.CHAR
import org.purescript.parser.COMMA
import org.purescript.parser.DARROW
import org.purescript.parser.DCOLON
import org.purescript.parser.DDOT
import org.purescript.parser.DOC_COMMENT
import org.purescript.parser.DOT
import org.purescript.parser.EQ
import org.purescript.parser.FLOAT
import org.purescript.parser.IDENT
import org.purescript.parser.LARROW
import org.purescript.parser.LDARROW
import org.purescript.parser.MLCOMMENT
import org.purescript.parser.MODULE_PREFIX
import org.purescript.parser.NATURAL
import org.purescript.parser.OPERATOR
import org.purescript.parser.OPTIMISTIC
import org.purescript.parser.PIPE
import org.purescript.parser.PROPER_NAME
import org.purescript.parser.SEMI
import org.purescript.parser.SLCOMMENT
import org.purescript.parser.STRING
import org.purescript.parser.STRING_ERROR
import org.purescript.parser.STRING_ESCAPED
import org.purescript.parser.STRING_GAP
import org.purescript.psi.PSForeignDataDeclaration
import org.purescript.psi.PSForeignValueDeclaration
import org.purescript.psi.module.PSModule
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.declaration.PSFixityDeclaration
import org.purescript.psi.declaration.PSValueDeclaration
import org.purescript.psi.imports.PSImportAlias
import org.purescript.psi.newtype.PSNewTypeConstructor
import org.purescript.psi.newtype.PSNewTypeDeclaration
import org.purescript.psi.typesynonym.PSTypeSynonymDeclaration

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is PSValueDeclaration
            || psiElement is PSVarBinder
            || psiElement is PSModule
            || psiElement is PSForeignValueDeclaration
            || psiElement is PSNewTypeDeclaration
            || psiElement is PSNewTypeConstructor
            || psiElement is PSImportAlias
            || psiElement is PSDataDeclaration
            || psiElement is PSDataConstructor
            || psiElement is PSClassDeclaration
            || psiElement is PSTypeSynonymDeclaration
            || psiElement is PSClassMember
            || psiElement is PSFixityDeclaration
            || psiElement is PSForeignDataDeclaration

    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
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

    override fun getHelpId(psiElement: PsiElement): String? {
        return null
    }

    override fun getType(element: PsiElement): String {
        return when (element) {
            is PSValueDeclaration -> "value"
            is PSVarBinder -> "parameter"
            is PSModule -> "module"
            is PSNewTypeDeclaration -> "newtype"
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
