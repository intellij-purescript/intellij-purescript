package net.kenro.ji.jin.purescript.parser

import net.kenro.ji.jin.purescript.parser.ParserContext
import net.kenro.ji.jin.purescript.parser.ParserInfo
import net.kenro.ji.jin.purescript.parser.Parsec
import net.kenro.ji.jin.purescript.parser.Combinators
import com.intellij.psi.tree.IElementType
import net.kenro.ji.jin.purescript.parser.SymbolicParsec
import java.util.LinkedHashSet
import java.lang.StringBuilder
import com.intellij.lang.PsiParser
import net.kenro.ji.jin.purescript.psi.PSTokens
import net.kenro.ji.jin.purescript.psi.PSElements
import com.intellij.lang.PsiBuilder
import net.kenro.ji.jin.purescript.parser.PureParser.PureParsecParser
import net.kenro.ji.jin.purescript.parser.ParsecRef
import com.intellij.lang.WhitespacesAndCommentsBinder
import com.intellij.lang.ParserDefinition
import net.kenro.ji.jin.purescript.lexer.PSLexer
import net.kenro.ji.jin.purescript.parser.PureParser
import com.intellij.psi.tree.IFileElementType
import net.kenro.ji.jin.purescript.file.PSFileStubType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.PsiElement
import net.kenro.ji.jin.purescript.psi.impl.PSProperNameImpl
import net.kenro.ji.jin.purescript.psi.impl.PSIdentifierImpl
import net.kenro.ji.jin.purescript.psi.impl.PSImportDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSDataDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSProgramImpl
import net.kenro.ji.jin.purescript.psi.impl.PSModuleImpl
import net.kenro.ji.jin.purescript.psi.impl.PSStarImpl
import net.kenro.ji.jin.purescript.psi.impl.PSBangImpl
import net.kenro.ji.jin.purescript.psi.impl.PSRowKindImpl
import net.kenro.ji.jin.purescript.psi.impl.PSFunKindImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeArgsImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeAnnotationNameImpl
import net.kenro.ji.jin.purescript.psi.impl.PSForAllImpl
import net.kenro.ji.jin.purescript.psi.impl.PSConstrainedTypeImpl
import net.kenro.ji.jin.purescript.psi.impl.PSRowImpl
import net.kenro.ji.jin.purescript.psi.impl.PSObjectTypeImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeVarImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeAtomImpl
import net.kenro.ji.jin.purescript.psi.impl.PSGenericIdentifierImpl
import net.kenro.ji.jin.purescript.psi.impl.PSLocalIdentifierImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeSynonymDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSValueDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSExternDataDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSExternInstanceDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSExternDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSFixityDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSPositionedDeclarationRefImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeClassDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeInstanceDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSNewTypeDeclarationImpl
import net.kenro.ji.jin.purescript.psi.impl.PSGuardImpl
import net.kenro.ji.jin.purescript.psi.impl.PSNullBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSStringBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSBooleanBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSNumberBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSNamedBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSVarBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSConstructorBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSObjectBinderImpl
import net.kenro.ji.jin.purescript.psi.impl.PSObjectBinderFieldImpl
import net.kenro.ji.jin.purescript.psi.impl.PSBinderAtomImpl
import net.kenro.ji.jin.purescript.psi.impl.PSValueRefImpl
import net.kenro.ji.jin.purescript.psi.impl.PSBooleanLiteralImpl
import net.kenro.ji.jin.purescript.psi.impl.PSNumericLiteralImpl
import net.kenro.ji.jin.purescript.psi.impl.PSStringLiteralImpl
import net.kenro.ji.jin.purescript.psi.impl.PSArrayLiteralImpl
import net.kenro.ji.jin.purescript.psi.impl.PSObjectLiteralImpl
import net.kenro.ji.jin.purescript.psi.impl.PSAbsImpl
import net.kenro.ji.jin.purescript.psi.impl.PSIdentInfixImpl
import net.kenro.ji.jin.purescript.psi.impl.PSVarImpl
import net.kenro.ji.jin.purescript.psi.impl.PSCaseImpl
import net.kenro.ji.jin.purescript.psi.impl.PSCaseAlternativeImpl
import net.kenro.ji.jin.purescript.psi.impl.PSIfThenElseImpl
import net.kenro.ji.jin.purescript.psi.impl.PSLetImpl
import net.kenro.ji.jin.purescript.psi.impl.PSParensImpl
import net.kenro.ji.jin.purescript.psi.impl.PSUnaryMinusImpl
import net.kenro.ji.jin.purescript.psi.impl.PSPrefixValueImpl
import net.kenro.ji.jin.purescript.psi.impl.PSAccessorImpl
import net.kenro.ji.jin.purescript.psi.impl.PSDoNotationLetImpl
import net.kenro.ji.jin.purescript.psi.impl.PSDoNotationBindImpl
import net.kenro.ji.jin.purescript.psi.impl.PSDoNotationValueImpl
import net.kenro.ji.jin.purescript.psi.impl.PSValueImpl
import net.kenro.ji.jin.purescript.psi.impl.PSFixityImpl
import net.kenro.ji.jin.purescript.psi.impl.PSJSRawImpl
import net.kenro.ji.jin.purescript.psi.impl.PSImpliesImpl
import net.kenro.ji.jin.purescript.psi.impl.PSTypeHoleImpl
import net.kenro.ji.jin.purescript.psi.cst.PSASTWrapperElement
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import net.kenro.ji.jin.purescript.file.PSFile
import com.intellij.lang.ParserDefinition.SpaceRequirements
import java.util.HashSet

class ParsecRef : Parsec() {
    private var ref: Parsec? = null
    fun setRef(ref: Parsec) {
        this.ref = ref
    }

    override fun parse(context: ParserContext): ParserInfo {
        return ref!!.parse(context)
    }

    public override fun calcName(): String {
        return ref!!.name!!
    }

    override fun calcExpectedName(): HashSet<String?> {
        return ref!!.expectedName!!
    }

    override fun canStartWith(type: IElementType): Boolean {
        return ref!!.canStartWith(type)
    }

    public override fun calcCanBeEmpty(): Boolean {
        return ref!!.canBeEmpty()
    }
}