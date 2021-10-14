package com.github.thahnen.config

import org.gradle.api.Project

import com.github.thahnen.EvaluateArtifactTaskException
import com.github.thahnen.EvaluateFilesOrFoldersException
import com.github.thahnen.GitDiffUpToDateObject
import com.github.thahnen.TaskConfigurationArtifactTaskNameInvalidException
import com.github.thahnen.TaskConfigurationFileOrFolderInvalidException
import com.github.thahnen.TaskConfigurationTaskNameInvalidException
import com.github.thahnen.t


/**
 *  Evaluator:
 *  =========
 *
 *  Provides functions to evaluate all part of any configuration contained in this plugin
 *
 *  @author thahnen
 */
class Evaluator {

    companion object {
        /**
         *  Evaluates if an input task provided in a configuration to this plugin does exist
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param configProperty the corresponding property
         *  @param config the configuration object parsed from property
         *  @throws TaskConfigurationTaskNameInvalidException when task was not found
         */
        @Throws(TaskConfigurationTaskNameInvalidException::class)
        internal fun evaluateInputTaskName(target: Project, configProperty: String, config: GitDiffUpToDateObject) {
            target.tasks.findByName(config.taskName) ?: run {
                throw TaskConfigurationTaskNameInvalidException(
                    "[GitDiffUpToDatePlugin -> ${this::class.simpleName}." +
                    "${this::evaluateInputTaskName.name}] Evaluating configuration from local gradle.properties file " +
                    "(property: '$configProperty') of project '${target.name}' contained the following configuration " +
                    "'$config' of which the input task could not be found!"
                )
            }
        }


        /**
         *  Evaluates if a given input / output file or folder provided in a configuration to this plugin does exist
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param configProperty the corresponding property
         *  @param config the configuration object parsed from property
         *  @param input true equals to input file or folder, false equals to output file or folder
         *  @throws TaskConfigurationFileOrFolderInvalidException when file or folder does not exist
         *  @throws EvaluateFilesOrFoldersException when I messed up - only a reminder to myself
         */
        @Throws(TaskConfigurationFileOrFolderInvalidException::class, EvaluateFilesOrFoldersException::class)
        internal fun evaluateFilesOrFolders(target: Project, configProperty: String, config: GitDiffUpToDateObject,
                                            input: Boolean) {
            val exceptionString = "[GitDiffUpToDatePlugin -> ${this::class.simpleName}." +
                                    "${this::evaluateFilesOrFolders.name}] Evaluating configuration from local " +
                                    "gradle.properties file (property: '$configProperty') of project " +
                                    "'${target.name}' contained the following ${input t "input" ?: "output"} file or " +
                                    "folder 'FOFTMP' which could not be found!"

            when (config) {
                is GitDiffUpToDateObject.AntInputOutputObject -> config.outputFilesOrFolders.forEach { fileOrFolder ->
                    if (!target.file("${target.projectDir}/$fileOrFolder").exists()) {
                        throw TaskConfigurationFileOrFolderInvalidException(
                            exceptionString.replace("FOFTMP", fileOrFolder)
                        )
                    }
                }
                is GitDiffUpToDateObject.GradleInputOutputObject -> config.outputFilesOrFolders.forEach { fileOrFolder ->
                    if (!target.file("${target.projectDir}/$fileOrFolder").exists()) {
                        throw TaskConfigurationFileOrFolderInvalidException(
                            exceptionString.replace("FOFTMP", fileOrFolder)
                        )
                    }
                }
                else -> throw EvaluateFilesOrFoldersException(
                    "[GitDiffUpToDatePlugin -> ${this::class.simpleName}.${this::evaluateFilesOrFolders.name}] Used " +
                    "wrong subtype of GitDiffUpToDateObject for parameter 'config' : ${config::class.qualifiedName}!"
                )
            }
        }


        /**
         *  Evaluates if an artifact task provided in a configuration to this plugin does exist
         *
         *  @param target the project which the plugin is applied to, may be sub-project
         *  @param configProperty the corresponding property
         *  @param config the configuration object parsed from property
         *  @throws TaskConfigurationArtifactTaskNameInvalidException when task was not found
         *  @throws EvaluateArtifactTaskException when I messed up - only a reminder to myself
         */
        @Throws(TaskConfigurationArtifactTaskNameInvalidException::class, EvaluateArtifactTaskException::class)
        internal fun evaluateArtifactTask(target: Project, configProperty: String, config: GitDiffUpToDateObject) {
            val exceptionString = "[GitDiffUpToDatePlugin -> ${this::class.simpleName}." +
                                    "${this::evaluateArtifactTask.name}] Evaluating configuration from local " +
                                    "gradle.properties file (property: '$configProperty') of project " +
                                    "'${target.name}' contained the following configuration '$config' of which the " +
                                    "artifact task could not be found!"

            when (config) {
                is GitDiffUpToDateObject.AntInputArtifactObject -> target.tasks.findByName(config.artifactTaskName) ?: run {
                    throw TaskConfigurationArtifactTaskNameInvalidException(exceptionString)
                }
                is GitDiffUpToDateObject.GradleInputArtifactObject -> target.tasks.findByName(config.artifactTaskName) ?: run {
                    throw TaskConfigurationArtifactTaskNameInvalidException(exceptionString)
                }
                else -> throw EvaluateArtifactTaskException(
                    "[GitDiffUpToDatePlugin -> ${this::class.simpleName}.${this::evaluateArtifactTask.name}] Used " +
                    "wrong subtype of GitDiffUpToDateObject for parameter 'config' : ${config::class.qualifiedName}!"
                )
            }
        }
    }
}
