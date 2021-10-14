package com.github.thahnen


/**
 *  GitDiffUpToDateObject:
 *  =====================
 *
 *  Object representing a Gradle task and its corresponding files / folders
 *
 *  @author thahnen
 */
open class GitDiffUpToDateObject private constructor(val taskName: String, val inputFilesOrFolders: Set<String>) {

    /** object for Ant task provided with input / output files or folders */
    class AntInputOutputObject(taskName: String, val propertyName: String, inputFilesOrFolders: Set<String>,
                               val outputFilesOrFolders: Set<String>) : GitDiffUpToDateObject(taskName, inputFilesOrFolders) {
        /// override toString function
        override fun toString(): String = "${this::class.simpleName} -> taskName = $taskName - " +
                                            "propertyName = $propertyName - " +
                                            "inputFilesOrFolders = $inputFilesOrFolders - " +
                                            "outputFilesOrFolders = $outputFilesOrFolders"
    }


    /** object for Ant task provided with input files or folders and Gradle artifact task */
    class AntInputArtifactObject(taskName: String, val propertyName: String, inputFilesOrFolders: Set<String>,
                                 val artifactTaskName: String) : GitDiffUpToDateObject(taskName, inputFilesOrFolders) {
        /// override toString function
        override fun toString(): String = "${this::class.simpleName} -> taskName = $taskName - " +
                                            "propertyName = $propertyName - " +
                                            "inputFilesOrFolders = $inputFilesOrFolders - " +
                                            "artifactTaskName = $artifactTaskName"
    }


    /** object for Gradle task provided with input / output files or folders */
    class GradleInputOutputObject(taskName: String, inputFilesOrFolders: Set<String>,
                                  val outputFilesOrFolders: Set<String>) : GitDiffUpToDateObject(taskName, inputFilesOrFolders) {
        /// override toString function
        override fun toString(): String = "${this::class.simpleName} -> taskName = $taskName - " +
                                            "inputFilesOrFolders = $inputFilesOrFolders - " +
                                            "outputFilesOrFolders = $outputFilesOrFolders"
    }


    /** object for Gradle task provided with input files or folders and artifact task */
    class GradleInputArtifactObject(taskName: String, inputFilesOrFolders: Set<String>,
                                    val artifactTaskName: String) : GitDiffUpToDateObject(taskName, inputFilesOrFolders) {
        /// override toString function
        override fun toString(): String = "${this::class.simpleName} -> taskName = $taskName - " +
                                            "inputFilesOrFolders = $inputFilesOrFolders - " +
                                            "artifactTaskName = $artifactTaskName"
    }
}
