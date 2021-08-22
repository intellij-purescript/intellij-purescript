package org.purescript.ide.purs

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.OSProcessUtil
import com.intellij.execution.process.OSProcessUtil.killProcessTree
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class StartupActivity : StartupActivity.Background {
    override fun runActivity(project: Project) {
        // without a project dir we don't know where to start the server
        val projectDir = project.guessProjectDir() ?: return
        val rootDir = projectDir.toNioPath()
        val pursBin = Purs().nodeModulesVersion(rootDir)
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