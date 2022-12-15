package org.purescript.ide.purs

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.xmlb.XmlSerializerUtil
import javax.swing.JComponent

class PursConfigurable(val project: Project) : Configurable {
    private val model = project.service<Purs>()

    private lateinit var panel: DialogPanel
    override fun createComponent(): JComponent {
        panel = panel {
            row("purs path") {
                textFieldWithBrowseButton(project = project).bindText(model::path)
            }
        }
        return panel
    }

    override fun isModified(): Boolean = panel.isModified()

    override fun apply() {
        panel.apply()
    }

    override fun reset() {
        panel.reset()
    }

    override fun getDisplayName(): String {
        return "Purs"
    }

}