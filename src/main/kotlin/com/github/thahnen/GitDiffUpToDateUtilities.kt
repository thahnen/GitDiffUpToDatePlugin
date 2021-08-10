package com.github.thahnen

import java.io.File
import java.util.concurrent.TimeUnit


/**
 *  Extension to Boolean to create a ternary operator
 *  -> <Condition> ? <to do if true> ?: <to do if false>
 */
internal infix fun <T: Any> Boolean.t(value: T) : T? = if (this) value else null


/**
 *  Extension to String to run a command on the command line and return if exit code was zero
 *  -> "git diff".invokeCommand(target.projectDir)
 *
 *  TODO: Handle exceptions?
 */
internal fun String.invokeCommand(workingDir: File) : Boolean {
    val process = ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
                    .directory(workingDir)
                    .start()
                    .also { it.waitFor(60, TimeUnit.SECONDS) }

    return process.exitValue() == 0
}
