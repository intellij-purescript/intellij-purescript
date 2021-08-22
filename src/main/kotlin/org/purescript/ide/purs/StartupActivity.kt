package org.purescript.ide.purs

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.StartupActivity

class StartupActivity : StartupActivity.Background {
    override fun runActivity(project: Project) {

        // without a project dir we don't know where to start the server
        val projectDir = project.guessProjectDir() ?: return
        val rootDir = projectDir.toNioPath()

        // without a purs bin path we can't annotate with it
        val pursBin = Purs().nodeModulesVersion(rootDir) ?: return

        val command = GeneralCommandLine(pursBin.toString(), "ide", "server")
            .withWorkDirectory(rootDir.toFile())
        val task = object : Task.Backgroundable(
            project,
            "purs ide server for " + project.name,
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                CapturingProcessHandler(command)
                    .runProcessWithProgressIndicator(indicator)
            }

        }
        task.queue()
    }
}