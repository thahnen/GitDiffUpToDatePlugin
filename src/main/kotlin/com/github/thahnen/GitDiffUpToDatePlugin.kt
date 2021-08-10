package com.github.thahnen

import org.gradle.api.Plugin
import org.gradle.api.Project


/**
 *  GitDiffUpToDatePlugin:
 *  =====================
 *
 *  Plugin to configure Gradle tasks passed to this plugin to be UP-TO-DATE depending on Git diff results on files /
 *  folders also provided to this plugin!
 *
 *  Result: - target.extensions.getByType(GitDiffUpToDatePluginExtension::class.java) for the following properties
 *          - tasks         -> set of GitDiffUpToDateObjects representing tasks and their files / folders
 *
 *  @author thahnen
 */
open class GitDiffUpToDatePlugin : Plugin<Project> {

    // identifiers of properties connected to this plugin
    private val KEY_CONFIG = "plugins.gitdiffuptodate.config"


    companion object {
        /**
         *  Parses the configuration property used by this plugin to set of GitDiffUpToDateObjects
         */
        internal fun parseTaskConfigurations(config: String) : Set<GitDiffUpToDateObject> {
            val taskConfigurations: MutableSet<GitDiffUpToDateObject> = mutableSetOf()

            config.trim().split(";").forEach {
                val (taskName: String, filesOrFolders: String) = it.split(":", limit = 2)

                taskConfigurations.add(GitDiffUpToDateObject(
                    taskName, filesOrFolders.split(",").map { fileOrFolder -> fileOrFolder.trim() }.toSet()
                ))
            }

            return taskConfigurations
        }
    }

    /** Overrides the abstract "apply" function */
    override fun apply(target: Project) {
        TODO("Not yet implemented")

        parseTaskConfigurations("").forEach {
            target.tasks.getByName(it.taskName) {
                outputs.upToDateWhen { task ->
                    it.filesOrFolders.all {
                        fileOrFolder -> Git.diff(target.projectDir, fileOrFolder)
                    } && target.file("${target.buildDir}/libs/${target.name}-${target.version}.jar").exists()
                }
            }
        }
    }
}
