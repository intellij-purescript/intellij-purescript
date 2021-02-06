package org.purescript.ide.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.actions.AttributesDefaults
import com.intellij.ide.fileTemplates.ui.CreateFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.purescript.icons.PSIcons
import java.nio.file.Paths
import java.util.*

class CreateFileAction : CreateFileFromTemplateAction(TITLE, "", PSIcons.FILE) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        /*
         *  If we add more kinds here, IntelliJ will prompt for which kind
         *  you want to create when invoking the action.
         *
         *  All template names must correspond to a file in [/src/main/resources/fileTemplates/internal],
         *  and to a <internalFileTemplate/> in [src/main/resources/META-INF/plugin.xml]
         */
        builder.setTitle(TITLE)
            .addKind("Module", PSIcons.FILE, "Purescript Module")
            .setValidator(NAME_VALIDATOR)
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
        TITLE

    /**
     * We override this so that we can control the properties
     * that are sent into the template. In particular, we want
     * `MODULE_NAME` to be available for the template engine.
     *
     * It looks a little hacky using the [CreateFromTemplateDialog] to create the file,
     * since we don't want any additional dialogs, but I couldn't find an easier way to do it.
     * The Julia plugin project is passing custom properties this way:
     *      [https://github.com/JuliaEditorSupport/julia-intellij/blob/master/src/org/ice1000/julia/lang/action/julia-file-actions.kt]
     */
    override fun createFileFromTemplate(name: String, template: FileTemplate, dir: PsiDirectory): PsiFile? {
        val lastModuleName = name.removeSuffix(".purs")
            .split(".")
            .last()
        val fileName = "$lastModuleName.purs"
        val properties = getProperties(lastModuleName, dir)

        // We need to set AttributesDefaults here, otherwise we will get prompted for the file name
        val dialog = CreateFromTemplateDialog(
            dir.project,
            dir,
            template,
            AttributesDefaults(fileName).withFixedName(true),
            properties
        )

        return dialog.create()?.containingFile
    }

    private fun getProperties(lastModuleName: String, dir: PsiDirectory): Properties {
        val moduleName = Paths.get(dir.virtualFile.path)
            .reversed()
            .takeWhile { "$it" != "src" && "$it" != "test" }
            .reversed()
            .joinToString(".")
            .let { "$it.$lastModuleName" }
            .removePrefix(".")

        val properties = FileTemplateManager.getInstance(dir.project).defaultProperties
        properties += "MODULE_NAME" to moduleName

        return properties
    }

    companion object {
        const val TITLE = "New Purescript File"

        internal val NAME_VALIDATOR = object : InputValidatorEx {
            override fun checkInput(inputString: String?): Boolean =
                true

            override fun canClose(inputString: String?): Boolean =
                getErrorText(inputString) == null

            override fun getErrorText(inputString: String?): String? {
                if (inputString.isNullOrBlank()) {
                    return "File name cannot be empty"
                }
                if (!inputString.first().isUpperCase()) {
                    return "File name must start with a capital letter"
                }
                return null
            }
        }
    }

    /**
     * Exposed only for testing purposes.
     */
    internal fun internalCreateFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        return createFile(name, templateName, dir)
    }
}
