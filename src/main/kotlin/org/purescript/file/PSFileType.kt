package org.purescript.file

import com.intellij.openapi.fileTypes.LanguageFileType
import org.purescript.PSLanguage
import org.purescript.icons.PSIcons

class PSFileType : LanguageFileType(PSLanguage.INSTANCE) {
    override fun getName() = "Purescript file"
    override fun getDescription() = "Purescript file"
    override fun getDefaultExtension() = "purs"
    override fun getIcon() = PSIcons.FILE
}

val PS_FILE_TYPE_INSTANCE = PSFileType()