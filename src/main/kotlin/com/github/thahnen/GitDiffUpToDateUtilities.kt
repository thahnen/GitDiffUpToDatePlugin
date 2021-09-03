package com.github.thahnen

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


/**
 *  Extension to Boolean to create a ternary operator
 *  -> <Condition> ? <to do if true> ?: <to do if false>
 */
@Suppress("unused")
internal infix fun <T: Any> Boolean.t(value: T) : T? = if (this) value else null


/**
 *  Extension to String to run a command on the command line and return if exit code was zero
 *  -> "git diff".invokeCommand(target.projectDir)
 */
internal fun String.invokeCommand(workingDir: File) : Boolean {
    val process = ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
                    .directory(workingDir)
                    .start()
                    .also { it.waitFor(60, TimeUnit.SECONDS) }

    return process.exitValue() == 0
}


/**
 *  Extension to String to run a command on the command line and return stdout
 *  -> "git log -n 1".invokeCommand(target.projectDir)
 */
internal fun String.invokeCommandWithOutput(workingDir: File) : String {
    val process = ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
                    .directory(workingDir)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .start()
                    .also { it.waitFor(60, TimeUnit.SECONDS) }

    when (process.exitValue()) {
        0   -> throw IOException("Exit value != 0 -> ${process.exitValue()}")
    }

    return process.inputStream.bufferedReader().readText()
}
