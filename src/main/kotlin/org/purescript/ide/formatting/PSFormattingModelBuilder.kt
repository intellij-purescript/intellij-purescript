package org.purescript.ide.formatting

import com.intellij.formatting.*
import com.intellij.formatting.Alignment.createAlignment
import com.intellij.formatting.FormattingModelProvider.createFormattingModelForPsiFile
import com.intellij.formatting.Wrap.createWrap
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.purescript.PSLanguage


class PSFormattingModelBuilder : FormattingModelBuilder {

    private fun createSpaceBuilder(settings: CodeStyleSettings): SpacingBuilder {
        return SpacingBuilder(settings, PSLanguage.INSTANCE)
    }

    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val psiFile = formattingContext.containingFile
        val node = formattingContext.node
        val styleSettings = formattingContext.codeStyleSettings
        
        val wrap = createWrap(WrapType.NONE, false)
        val alignment = createAlignment()
        val spacingBuilder = createSpaceBuilder(styleSettings)
        val block = PSBlock(node, wrap, alignment, spacingBuilder)
        
        return createFormattingModelForPsiFile(psiFile, block, styleSettings)
    }
}