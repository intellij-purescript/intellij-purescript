package org.purescript.file

import com.intellij.openapi.fileTypes.LanguageFileType
import org.purescript.PSLanguage
import org.purescript.icons.PSIcons
import javax.swing.Icon

class PSFileType private constructor() : LanguageFileType(PSLanguage.INSTANCE) {
    override fun getName(): String {
        return "Purescript file"
    }

    override fun getDescription(): String {
        return "Purescript file"
    }

    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon {
        return PSIcons.FILE
    }

    companion object {
        val INSTANCE = PSFileType()
        const val DEFAULT_EXTENSION = "purs"
    }
}