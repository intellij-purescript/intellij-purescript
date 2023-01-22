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
import org.purescript.parser.LOWER
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
import org.purescript.psi.declaration.foreign.PSForeignDataDeclaration
import org.purescript.psi.declaration.foreign.ForeignValueDecl
import org.purescript.psi.module.Module
import org.purescript.psi.binder.PSVarBinder
import org.purescript.psi.declaration.classes.ClassDecl
import org.purescript.psi.declaration.classes.PSClassMember
import org.purescript.psi.declaration.data.DataConstructor
import org.purescript.psi.declaration.data.DataDeclaration
import org.purescript.psi.declaration.fixity.FixityDeclaration
import org.purescript.psi.declaration.imports.PSImportAlias
import org.purescript.psi.declaration.newtype.NewtypeCtor
import org.purescript.psi.declaration.newtype.NewtypeDecl
import org.purescript.psi.declaration.type.TypeDecl
import org.purescript.psi.declaration.value.ValueDecl

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is ValueDecl
            || psiElement is PSVarBinder
            || psiElement is Module.Psi
            || psiElement is ForeignValueDecl
            || psiElement is NewtypeDecl
            || psiElement is NewtypeCtor
            || psiElement is PSImportAlias
            || psiElement is DataDeclaration.Psi
            || psiElement is DataConstructor.Psi
            || psiElement is ClassDecl
            || psiElement is TypeDecl
            || psiElement is PSClassMember
            || psiElement is FixityDeclaration
            || psiElement is PSForeignDataDeclaration

    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
        PSLexer(),
        TokenSet.create(LOWER, PROPER_NAME, MODULE_PREFIX),
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
            is ValueDecl -> "value"
            is PSVarBinder -> "parameter"
            is Module.Psi -> "module"
            is NewtypeDecl -> "newtype"
            is NewtypeCtor -> "newtype constructor"
            is PSImportAlias -> "import alias"
            is DataDeclaration.Psi -> "data"
            is DataConstructor.Psi -> "data constructor"
            is ClassDecl -> "class"
            is ForeignValueDecl -> "foreign value"
            is PSForeignDataDeclaration -> "foreign data"
            is TypeDecl -> "type synonym"
            is PSClassMember -> "class member"
            is FixityDeclaration -> "operator"
            else -> "unknown"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        when (element) {
            is ValueDecl -> {
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
