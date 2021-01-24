package org.purescript

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition
import org.wso2.lsp4intellij.IntellijLanguageClient
import java.io.File

class PSStartupActivity: StartupActivity.DumbAware {

    override fun runActivity(project: Project) {

        val cmdCommand = when {
            com.intellij.openapi.util.SystemInfo.isWindows ->
                "${project.basePath}/node_modules/.bin/purescript-language-server.cmd"
            else ->
                "${project.basePath}/node_modules/.bin/purescript-language-server"
        }
        if (File(cmdCommand).exists()) {
            val command = arrayOf(
                cmdCommand,
                "--stdio"
            )
            val ext = "purs"
            val commandDefinition = RawCommandServerDefinition(ext, command)
            IntellijLanguageClient.addServerDefinition(commandDefinition, project)
        }
    }
}