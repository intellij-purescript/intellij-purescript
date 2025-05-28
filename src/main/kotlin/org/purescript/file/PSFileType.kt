package org.purescript.file

import com.intellij.openapi.fileTypes.LanguageFileType
import org.purescript.PSLanguage
import org.purescript.icons.PSIcons
import javax.swing.Icon

object PSFileType : LanguageFileType(PSLanguage) {
    override fun getName(): String {
        return "Purescript file"
    }
    override fun getDescription(): String = "Purescript file"
    override fun getDefaultExtension(): String {
        return "purs"
    }
    override fun getIcon(): Icon = PSIcons.FILE


}