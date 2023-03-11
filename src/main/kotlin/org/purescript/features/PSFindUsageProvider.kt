package org.purescript.features

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import org.purescript.lexer.PSLexer
import org.purescript.module.Module
import org.purescript.module.declaration.classes.ClassDecl
import org.purescript.module.declaration.classes.PSClassMember
import org.purescript.module.declaration.data.DataConstructor
import org.purescript.module.declaration.data.DataDeclaration
import org.purescript.module.declaration.fixity.FixityDeclaration
import org.purescript.module.declaration.foreign.ForeignValueDecl
import org.purescript.module.declaration.foreign.PSForeignDataDeclaration
import org.purescript.module.declaration.imports.PSImportAlias
import org.purescript.module.declaration.newtype.NewtypeCtor
import org.purescript.module.declaration.newtype.NewtypeDecl
import org.purescript.module.declaration.type.TypeDecl
import org.purescript.module.declaration.value.ValueDeclarationGroup
import org.purescript.module.declaration.value.binder.VarBinder
import org.purescript.parser.*

class PSFindUsageProvider : FindUsagesProvider {
    override fun canFindUsagesFor(psiElement: PsiElement): Boolean =
        psiElement is ValueDeclarationGroup
            || psiElement is VarBinder
            || psiElement is Module
            || psiElement is ForeignValueDecl
            || psiElement is NewtypeDecl
            || psiElement is NewtypeCtor
            || psiElement is PSImportAlias
            || psiElement is DataDeclaration
            || psiElement is DataConstructor
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
            is ValueDeclarationGroup -> "value"
            is VarBinder -> "parameter"
            is Module -> "module"
            is NewtypeDecl -> "newtype"
            is NewtypeCtor -> "newtype constructor"
            is PSImportAlias -> "import alias"
            is DataDeclaration -> "data"
            is DataConstructor -> "data constructor"
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
            is ValueDeclarationGroup -> {
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
