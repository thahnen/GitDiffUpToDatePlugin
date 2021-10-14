package com.github.thahnen

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

import org.gradle.api.Project


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

    if (process.exitValue() != 0) {
        throw IOException("Exit value != 0 -> ${process.exitValue()}")
    }

    return process.inputStream.bufferedReader().readText().trim()
}


/**
 *  Flattens a set of files excluding empty directories
 *
 *  @return set of flattened files
 */
internal fun Set<File>.flatten() : Set<File> {

    /** internal function to flatten a single file */
    fun File.flatten() : Set<File>? {
        when {
            !this.isDirectory || (this.isDirectory && this.listFiles()!!.isEmpty()) -> return null
        }

        val files: MutableSet<File> = mutableSetOf()
        this.listFiles()!!.forEach {
            when {
                it.isFile   -> files.add(it.absoluteFile)
                else        -> it.flatten()?.let { output -> files.addAll(output) }
            }
        }

        return files
    }


    val files: MutableSet<File> = mutableSetOf()
    this.forEach {
        when {
            it.isFile       -> files.add(it)
            it.isDirectory  -> it.flatten()?.let { output -> files.addAll(output) }
        }
    }

    return files
}


/**
 *  Extension to set of strings to flatten the potential file paths to actual files
 *
 *  @param target Gradle project necessary for local files
 *  @return set of flattened files or folders
 */
internal fun Set<String>.flattenFilesOrFolders(target: Project) : Set<File> = this.map {
    target.file("${target.projectDir}/$it")
}.toSet().flatten()


/**
 *  Checks on multiple parameters if they are null. Opposite of "multipleLet" ;)
 */
internal fun <T: Any> multipleNull(vararg elements: T?, closure: (List<T?>) -> Unit) {
    if (elements.all { it == null }) {
        closure(elements.filter { it == null })
    }
}


/**
 *  Extension to Boolean to create a ternary operator
 *  -> <Condition> ? <to do if true> ?: <to do if false>
 */
internal infix fun <T: Any> Boolean.t(value: T) : T? = if (this) value else null
