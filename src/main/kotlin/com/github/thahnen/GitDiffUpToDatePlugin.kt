package com.github.thahnen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar


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
         *
         *  @param config necessary properties entry (not empty / blank)
         *  @return set of task configurations (task -> Set<file / folder path>
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
        // 1) check if Java plugin applied to target (necessary because check on Jar task)
        if (!target.plugins.hasPlugin(JavaPlugin::class.java)) {
            throw PluginAppliedUnnecessarilyException("Plugin shouldn't be applied when Java plugin isn't used!")
        }

        // 2) retrieve necessary properties entry
        val config = getPropertiesEntry(target)
        if (config.isEmpty() || config.isBlank()) {
            throw PropertiesEntryInvalidException("$KEY_CONFIG provided but invalid (empty / blank)!")
        }

        // 3) parse configuration string to actual task configuration & evaluate correctness
        val taskConfigurations = parseTaskConfigurations(config)
        taskConfigurations.forEach {
            target.tasks.findByName(it.taskName) ?: run {
                throw TaskConfigurationTaskNameInvalidException(
                    "Task ${it.taskName} could not be found for configuration in project ${target.name}!"
                )
            }

            it.filesOrFolders.forEach { fileOrFolder ->
                if (!target.file(fileOrFolder).exists()) {
                    throw TaskConfigurationFileOrFolderInvalidException(
                        "File / folder $fileOrFolder  connected to task ${it.taskName} does not exist"
                    )
                }
            }
        }

        // 4) configure each task based on its files / folders
        taskConfigurations.forEach { task ->
            target.tasks.getByName(task.taskName) {
                outputs.upToDateWhen {
                    target.tasks.withType(Jar::class.java).toList().all { jarTask ->
                        target.file("${target.buildDir}/libs/${jarTask.archiveFileName.get()}").exists()
                    } && task.filesOrFolders.all { fileOrFolder ->
                        Git.diff(target.projectDir, fileOrFolder)
                    }
                }
            }
        }
    }


    /**
     *  Tries to retrieve the necessary properties file entry (can be provided as system property / environment variable
     *  as well)
     *
     *  @param target the project which the plugin is applied to, may be sub-project
     *  @return content of properties entry
     *  @throws MissingPropertiesEntryException when necessary properties entry not found / provided
     */
    @Throws(MissingPropertiesEntryException::class)
    private fun getPropertiesEntry(target: Project) : String {
        return when {
            target.properties.containsKey(KEY_CONFIG)               -> target.properties[KEY_CONFIG]
            target.rootProject.properties.containsKey(KEY_CONFIG)   -> target.rootProject.properties[KEY_CONFIG]
            System.getProperties().containsKey(KEY_CONFIG)          -> System.getProperties()[KEY_CONFIG]
            System.getenv().containsKey(KEY_CONFIG)                 -> System.getenv(KEY_CONFIG)
            else                                                    -> throw MissingPropertiesEntryException(
                                                                            "$KEY_CONFIG not provided to plugin!"
                                                                        )
        } as String
    }
}
